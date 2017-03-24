package nl.arnom.jenkins.flashsize.parsers;

import nl.arnom.jenkins.flashsize.FileSize;
import nl.arnom.jenkins.flashsize.SizeEntry;
import nl.arnom.jenkins.flashsize.ToolParser;

public class BasicSizeEntry implements SizeEntry {

  private String parser;
  private String fileName;
  private String sectionName;
  private FileSize size;

  public BasicSizeEntry(String parser, String fileName, String sectionName, Long sizeInBytes) {
    this.parser = parser;
    this.fileName = fileName;
    this.sectionName = sectionName;
    this.size = (sizeInBytes == null) ? null : new FileSize(sizeInBytes);
  }

  public BasicSizeEntry(ToolParser parser, String fileName, String sectionName, Long sizeInBytes) {
    this((parser == null) ? null : parser.getUniqueIdentifier(), fileName, sectionName, sizeInBytes);
  }

  public BasicSizeEntry() {
    this.parser = null;
    this.fileName = null;
    this.sectionName = null;
    this.size = null;
  }

  public String getParserId() {
    return parser;
  }

  public void setParser(ToolParser parser) {
    this.parser = (parser == null) ? null : parser.getUniqueIdentifier();
  }

  public void setParser(String parser) {
    this.parser = parser;
  }

  @Override
  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  @Override
  public String getSectionName() {
    return sectionName;
  }

  public void setSectionName(String sectionName) {
    this.sectionName = sectionName;
  }

  @Override
  public FileSize getSize() {
    return size;
  }

  public void setSize(FileSize size) {
    this.size = size;
  }

  public void setSize(long sizeInBytes) {
    this.size = new FileSize(sizeInBytes);
  }
}
