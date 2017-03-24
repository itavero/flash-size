package nl.arnom.jenkins.flashsize;

import java.text.NumberFormat;

/**
 * Created by amoonen on 27-3-2017.
 */
public class FileSize implements Comparable<FileSize> {

  private final long bytes;

  public FileSize(long bytes) {
    this.bytes = bytes;
  }

  public String toHumanReadableString(int decimals, boolean si) {
    boolean isNegative = (bytes < 0);
    long input = (isNegative) ? (bytes * -1) : (bytes);
    final int unit = si ? 1000 : 1024;
    if (input < unit) {
      return bytes + " B";
    }
    int exp = (int) (Math.log(input) / Math.log(unit));
    final String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
    return String.format("%." + Math.abs(decimals) + "f %sB", bytes / Math.pow(unit, exp), pre);
  }

  public String toString() {
    return toHumanReadableString(2, false);
  }

  public String toBytesString() {
    final NumberFormat format = NumberFormat.getIntegerInstance();
    return format.format(bytes) + " bytes";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FileSize)) {
      return false;
    }

    FileSize fileSize = (FileSize) obj;

    return bytes == fileSize.bytes;
  }

  @Override
  public int hashCode() {
    return (int) (bytes ^ (bytes >>> 32));
  }

  public long asLong() {
    return bytes;
  }

  @Override
  public int compareTo(FileSize o) {
    if (o == null) {
      return 1;
    }
    return new Long(bytes).compareTo(o.asLong());
  }
}
