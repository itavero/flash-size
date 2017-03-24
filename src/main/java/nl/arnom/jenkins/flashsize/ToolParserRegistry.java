package nl.arnom.jenkins.flashsize;

import com.google.common.collect.Sets;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

/**
 * Registry of all available ToolParser classes,
 * including those provided by other plugins.
 *
 * @author Arno Moonen
 */
public class ToolParserRegistry {

  private final Set<ToolParser> parsers;

  /**
   * Constructor
   *
   * @param parsers List of parsers (may be empty to get all available parsers)
   * @param charset Charset used when reading files
   */
  public ToolParserRegistry(final Collection<ToolParser> parsers) {
    this.parsers = new HashSet<ToolParser>();
    if (CollectionUtils.isNotEmpty(parsers)) {
      this.parsers.addAll(parsers);
    }
    if (this.parsers.isEmpty()) {
      this.parsers.addAll(all());
    }
  }


  public ToolParserRegistry() {
    this(null);
  }

  /**
   * Get a set containing all available parsers.
   *
   * @return All available parsers
   */
  public static Set<ToolParser> all() {
    final Jenkins instance = Jenkins.getInstance();
    if (instance == null) {
      return new HashSet<ToolParser>();
    }
    return Sets.newHashSet(instance.getExtensionList(ToolParser.class));
  }

  /**
   * Get a sorted set, containing all available parsers.
   *
   * @return All available parsers, sorted by name.
   */
  public static SortedSet<ToolParser> allSorted() {
    final Jenkins instance = Jenkins.getInstance();
    final TreeSet<ToolParser> result = new TreeSet<ToolParser>(new ToolParserComparator());
    if (instance == null) {
      return result;
    }
    result.addAll(instance.getExtensionList(ToolParser.class));
    return result;
  }

  /**
   * Get all parsers as a ListBoxModel.
   * This method can be used when generating items for a combobox in the UI.
   */
  public static ListBoxModel getParsersAsListModel() {
    ListBoxModel items = new ListBoxModel();
    for (ToolParser parser : allSorted()) {
      items.add(parser.getToolName(), parser.getUniqueIdentifier());
    }
    return items;
  }

  /**
   * Get parser with the given ID from the registry.
   * If no parser with the given ID is found, it will return null.
   *
   * @param id Identifier as returned by ToolParser#getUniqueIdentifier()
   *
   * @return ToolParser instance (or null, if none was found)
   *
   * @see ToolParser#getUniqueIdentifier()
   */
  public ToolParser getParserById(final String id) {
    if (id == null) {
      return null;
    }

    final String trimmedId = id.trim();
    if (trimmedId.isEmpty()) {
      return null;
    }

    ToolParser caseInsensitiveMatch = null;
    for (ToolParser t : parsers) {
      final String tId = t.getUniqueIdentifier().trim();
      if (tId.equals(trimmedId)) {
        return t;
      }
      if (tId.equalsIgnoreCase(trimmedId)) {
        caseInsensitiveMatch = t;
      }
    }
    return caseInsensitiveMatch;
  }

}
