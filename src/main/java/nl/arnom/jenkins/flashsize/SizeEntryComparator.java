package nl.arnom.jenkins.flashsize;

import org.apache.commons.lang.ObjectUtils;

import java.util.Comparator;

/**
 * Created by amoonen on 27-3-2017.
 */
public class SizeEntryComparator implements Comparator<SizeEntry> {
  public int compare(SizeEntry first, SizeEntry second) {
    if (first == null) {
      return (second == null) ? 0 : -1;
    }
    if (second == null) {
      return 1;
    }
    if (first.equals(second)) {
      return 0;
    }

    // Compare file names
    int c = ObjectUtils.compare(first.getFileName(), second.getFileName());
    if (c != 0) {
      return c;
    }

    // Compare section
    c = ObjectUtils.compare(first.getSectionName(), second.getSectionName());
    if (c != 0) {
      return c;
    }

    // Compare size
    c = ObjectUtils.compare(first.getSize(), second.getSize());
    if (c != 0) {
      return c;
    }

    // Compare tool
    return ObjectUtils.compare(first.getParserId(), second.getParserId());
  }
}
