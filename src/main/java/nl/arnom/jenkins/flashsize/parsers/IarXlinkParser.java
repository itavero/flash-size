package nl.arnom.jenkins.flashsize.parsers;

import hudson.Extension;
import hudson.model.TaskListener;
import nl.arnom.jenkins.flashsize.SizeEntry;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Created by amoonen on 24-3-2017.
 */
@Extension
public class IarXlinkParser extends AbstractToolParser {

  public IarXlinkParser() {
    super("IAR AVR Xlink");
  }

  @Override
  public Collection<SizeEntry> parse(TaskListener listener, Reader reader, boolean isConsole) throws IOException {
    List<SizeEntry> results = new ArrayList<SizeEntry>();

    if (reader == null) {
      return results;
    }

    final Pattern patternStart = Pattern.compile("xlink(\\.exe)?.*-o\\s*([^\\s]+)");
    final Pattern patternMemory = Pattern.compile("^[\\t ]*([\\d ]+) *bytes of ([^\\s]+)\\s+memory\\s+(\\([^)]+\\))?", Pattern.MULTILINE);
    final Pattern patternEnd = Pattern.compile("(Errors:|Warnings:|xlink(\\.exe)|iccavr(\\.exe))");
    final Scanner scanner = new Scanner(reader);

    while (scanner.findWithinHorizon(patternStart, 0) != null) {
      // Found a call to xlink
      final MatchResult start = scanner.match();
      final String file = start.group(2);
      boolean foundAnEntry = false;

      // Read line by line until end
      while (scanner.hasNextLine()) {
        if (scanner.findInLine(patternEnd) != null) {
          // End reached
          break;
        }

        if (scanner.findInLine(patternMemory) != null) {
          // Memory size found
          try {
            final MatchResult memory = scanner.match();
            final String sizeAsText = memory.group(1).replaceAll("[^0-9]+", "");
            final String section = memory.group(2);
            final Long size = Long.parseLong(sizeAsText);
            results.add(new BasicSizeEntry(this, file, section, size));
            foundAnEntry = true;
          } catch (Throwable t) {
            listener.error(t.getClass().getSimpleName() + " raised while scanning sizes for file '"
                               + file + "': " + t.getMessage());
          }
          continue;
        }

        // Skip this line
        scanner.nextLine();
      }

      // Check if entries found
      if (!foundAnEntry) {
        listener.error("No sizes found for file: " + file);
      }
    }

    return results;
  }
}
