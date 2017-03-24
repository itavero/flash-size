package nl.arnom.jenkins.flashsize.parsers;

import hudson.Extension;
import hudson.model.TaskListener;
import nl.arnom.jenkins.flashsize.SizeEntry;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

@Extension
public class DummyParser extends AbstractToolParser {

  private static final String[] names = { "foo", "bar" };
  private static final String[] sections = { "code", "const", "lib", "root", "init", "data" };
  private static final String[] extensions = { "bin", "hex" };

  public DummyParser() {
    super("Dummy Parser That Makes Up Sizes");
  }

  @Override
  public Collection<SizeEntry> parse(TaskListener listener, Reader reader, boolean isConsole) throws IOException {
    final Random random = new Random();
    final int numOfEntries = random.nextInt(30) + 1;
    final List<SizeEntry> result = new ArrayList<SizeEntry>(numOfEntries);
    for (int i = 0; i < numOfEntries; ++i) {
      result.add(generateRandomSizeEntry(random));
    }
    return result;
  }

  private SizeEntry generateRandomSizeEntry(Random random) {
    final String fileName = names[random.nextInt(names.length)] + '.' + extensions[random.nextInt(extensions.length)];
    final String section = sections[random.nextInt(sections.length)];
    final long size = (long) (Math.pow(2, random.nextInt(16) + 8) + random.nextInt(1048576));
    return new BasicSizeEntry(this, fileName, section, size);
  }
}
