package nl.arnom.jenkins.flashsize;

import java.util.Comparator;

/**
 * Comparator used for sorting ToolParser instances.
 * Sorting is done based on their display name.
 */
public class ToolParserComparator implements Comparator<ToolParser> {
  @Override
  public int compare(ToolParser first, ToolParser second) {
    if (first == null) {
      return (second == null) ? 0 : -1;
    }
    if (second == null) {
      return 1;
    }
    if (first.equals(second)) {
      return 0;
    }
    int nameCompare = second.getToolName().compareTo(first.getToolName());
    if (nameCompare != 0) {
      return nameCompare;
    }
    return second.getClass().getCanonicalName().compareTo(first.getClass().getCanonicalName());
  }
}
