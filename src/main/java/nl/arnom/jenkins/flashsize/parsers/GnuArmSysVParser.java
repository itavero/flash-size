package nl.arnom.jenkins.flashsize.parsers;

import hudson.Extension;
import hudson.model.TaskListener;
import nl.arnom.jenkins.flashsize.SizeEntry;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by amoonen on 24-3-2017.
 */
@Extension
public class GnuArmSysVParser extends AbstractToolParser {

  public GnuArmSysVParser() {
    super("arm-none-eabi-size -A");
  }

  @Override
  public Collection<SizeEntry> parse(TaskListener listener, Reader reader, boolean isConsole) throws IOException {
    List<SizeEntry> results = new ArrayList<SizeEntry>();

    if (reader == null) {
      return results;
    }

    final Pattern patternStart = Pattern.compile("arm-none-eabi-size(\\.exe)?((\\s+-A|\\s+--format=SysV|\\s+-d|\\s+-o|\\s+-x|\\s+--radix=\\d{1,2})+)\\s+.*", Pattern.CASE_INSENSITIVE);
    final Pattern patternFile = Pattern.compile("^([^ ]+)  : *$", Pattern.MULTILINE);
    final Pattern patternHeader = Pattern.compile("^section +size +addr$", Pattern.MULTILINE);
    final Pattern patternMemory = Pattern.compile("^.([^ ]+) +([0-9a-z]+) +([0-9a-z]+) *$", Pattern.MULTILINE);
    final Pattern patternEnd = Pattern.compile("^Total +([0-9a-z]+)", Pattern.MULTILINE);
    final Pattern patternExtractRadius = Pattern.compile("--radix=(\\d{1,2})");
    final Scanner scanner = new Scanner(reader);

    while (scanner.findWithinHorizon(patternStart, 0) != null) {
      // Found a call to size
      String fileName = null;
      final MatchResult start = scanner.match();
      String options = start.group(2).trim();
      if (!options.contains("-A") && !options.toLowerCase().contains("--format=sysv")) {
        // Illegal format
        continue;
      }

      // Determine options
      int radix = 10;
      if (options.contains("-d")) {
        radix = 10;
      } else if (options.contains("-o")) {
        radix = 8;
      } else if (options.contains("-x")) {
        radix = 16;
      } else {
        final Matcher radixMatcher = patternExtractRadius.matcher(options);
        if (radixMatcher.find()) {
          final String str = radixMatcher.group(1);
          radix = Integer.parseInt(str);
        }
      }

      boolean foundAnEntry = false;
      boolean headerSeen = false;

      // Read line by line until end
      while (scanner.hasNextLine()) {
        if (fileName == null) {
          if (scanner.findInLine(patternFile) != null) {
            // Found new file
            final MatchResult file = scanner.match();
            fileName = file.group(1);
            headerSeen = false;
            continue;
          }
        } else {
          if (headerSeen) {
            if (scanner.findInLine(patternEnd) != null) {
              // Reset search
              fileName = null;
              headerSeen = false;
              continue;
            }
            if (scanner.findInLine(patternMemory) != null) {
              // Found section
              try {
                final MatchResult section = scanner.match();
                final String name = section.group(1);
                final String sizeAsText = section.group(2).toLowerCase().replaceAll("[^0-9a-z]+", "");
                final Long size = Long.parseLong(sizeAsText, radix);
                results.add(new BasicSizeEntry(this, fileName, name, size));
                foundAnEntry = true;
              } catch (Throwable t) {
                listener.error(t.getClass().getSimpleName() + " raised while scanning sizes for file '"
                                   + fileName + "': " + t.getMessage());
              }
              continue;
            }
          } else {
            if (scanner.findInLine(patternHeader) != null) {
              // Reset search
              headerSeen = true;
              continue;
            }
          }
        }

        String line = scanner.nextLine().trim();
        if (!line.isEmpty()) {
          break;
        }
      }

      // Check if entries found
      if (!foundAnEntry) {
        listener.error("No sizes found after call to size tool.");
      }
    }

    return results;
  }
}
