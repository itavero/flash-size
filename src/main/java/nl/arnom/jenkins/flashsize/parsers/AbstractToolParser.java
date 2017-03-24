package nl.arnom.jenkins.flashsize.parsers;

import nl.arnom.jenkins.flashsize.ToolParser;

/**
 * Shared base for (built-in) parsers.
 *
 * @author Arno Moonen
 */
public abstract class AbstractToolParser implements ToolParser {
  private final String toolName;
  private final String uniqueId;

  protected AbstractToolParser(String toolName) {
    this.toolName = toolName;
    this.uniqueId = generateUniqueId(this.getClass());
  }

  private static String generateUniqueId(Class<? extends AbstractToolParser> cl) {
    if (AbstractToolParser.class.getPackage().equals(cl.getPackage())) {
      return cl.getSimpleName();
    }
    return cl.getCanonicalName();
  }

  @Override
  public String getToolName() {
    return toolName;
  }

  @Override
  public String getUniqueIdentifier() {
    return uniqueId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ToolParser)) {
      return false;
    }

    ToolParser that = (ToolParser) o;
    return this.getUniqueIdentifier().equals(that.getUniqueIdentifier());
  }

  @Override
  public int hashCode() {
    ;
    return uniqueId != null ? uniqueId.hashCode() : 0;
  }
}
