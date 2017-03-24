package nl.arnom.jenkins.flashsize;

import java.io.Serializable;

public interface SizeEntry extends Serializable {
  String getParserId();

  String getFileName();

  String getSectionName();

  FileSize getSize();
}
