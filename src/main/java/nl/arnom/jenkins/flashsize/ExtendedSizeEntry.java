package nl.arnom.jenkins.flashsize;

/**
 * Created by arnom on 30/03/2017.
 */
public class ExtendedSizeEntry implements SizeEntry {
  private final SizeEntry entry;
  private final ToolParser parser;

  public ExtendedSizeEntry(SizeEntry entry, ToolParser parser) {
    this.entry = entry;
    this.parser = parser;
  }

  public ToolParser getParser() {
    return this.parser;
  }

  public String getParserId() {
    return this.entry.getParserId();
  }

  public String getFileName() {
    return this.entry.getFileName();
  }

  public String getSectionName() {
    return this.entry.getSectionName();
  }

  public FileSize getSize() {
    return this.entry.getSize();
  }
}
