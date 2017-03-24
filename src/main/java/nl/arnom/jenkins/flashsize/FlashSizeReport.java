package nl.arnom.jenkins.flashsize;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

public class FlashSizeReport implements Serializable {

  private transient static final Logger logger = Logger.getLogger(BuildAction.class.getName());
  private final Collection<SizeEntry> entries;
  private final Collection<String> includedSections;
  private final Collection<String> excludedSections;
  private transient ToolParserRegistry parserRegistry;
  private transient StringMap<StringMap<SizeEntry>> lookUpMap;

  public FlashSizeReport(Collection<String> includedSections, Collection<String> excludedSections) {
    this.includedSections = includedSections;
    this.excludedSections = excludedSections;
    this.entries = new HashSet<SizeEntry>();
    this.lookUpMap = new StringMap<StringMap<SizeEntry>>();
  }

  public Collection<String> getFiles() {
    return Collections.unmodifiableSortedSet(new TreeSet<String>(lookUpMap.keySet()));
  }

  public String getStrippedName(final String input) {
    if (input == null) {
      return "null";
    }
    return StringUtils.strip(input.replaceAll("[^A-Za-z0-9]", "_"), "_");
  }

  public Set<String> getToolIdentifiersForFile(String file) {
    final StringMap<SizeEntry> sections = lookUpMap.get(file);
    if (sections == null) {
      throw new IllegalArgumentException("Unknown file: " + file);
    }

    final Set<String> tools = new HashSet<String>();
    for (SizeEntry entry : sections.values()) {
      tools.add(entry.getParserId());
    }
    return tools;
  }

  public int getToolCountForFile(String file) {
    return getToolIdentifiersForFile(file).size();
  }

  public FileSize getTotalSizeOfFile(String file) {
    final StringMap<SizeEntry> sections = lookUpMap.get(file);
    if (sections == null) {
      throw new IllegalArgumentException("Unknown file: " + file);
    }

    long totalSize = 0;
    for (SizeEntry entry : filterOutSections(sections.values())) {
      totalSize += entry.getSize().asLong();
    }
    return new FileSize(totalSize);
  }

  private Set<SizeEntry> filterOutSections(final Collection<SizeEntry> entries) {
    Set<SizeEntry> result = new HashSet<SizeEntry>(entries.size());
    for (SizeEntry entry : entries) {
      if (sectionIsIncluded(entry.getSectionName())) {
        result.add(entry);
      }
    }
    return result;
  }

  private boolean sectionFilterMatches(String filter, String input) {
    if (StringUtils.isEmpty(filter) || StringUtils.isEmpty(input)) {
      return false;
    }
    final int star = filter.indexOf('*');
    if (star < 0) {
      return input.equalsIgnoreCase(filter);
    }
    if (star == 0) {
      // Stars with star
      final String ending = filter.substring(1).toLowerCase();
      return !ending.isEmpty() && input.toLowerCase().endsWith(ending);
    }
    if (star == (filter.length() - 1)) {
      // Ends with star
      final String start = filter.substring(0, star).toLowerCase();
      return !start.isEmpty() && input.toLowerCase().startsWith(start);
    }
    return false;
  }

  public boolean sectionIsIncluded(final String input) {
    if (input == null) {
      return false;
    }

    boolean isIncluded = CollectionUtils.isEmpty(includedSections);

    if (isIncluded && CollectionUtils.isEmpty(excludedSections)) {
      // No patterns set. Everything included.
      return true;
    }

    if (!isIncluded) {
      for (String filter : includedSections) {
        if (sectionFilterMatches(filter, input)) {
          isIncluded = true;
          break;
        }
      }
    }

    if (isIncluded) {
      for (String filter : excludedSections) {
        if (sectionFilterMatches(filter, input)) {
          isIncluded = false;
          break;
        }
      }
    }

    return isIncluded;
  }

  public Collection<String> getSections(String file) {
    final StringMap<SizeEntry> sections = lookUpMap.get(file);
    if (sections == null) {
      throw new IllegalArgumentException("Unknown file: " + file);
    }
    return Collections.unmodifiableSortedSet(new TreeSet<String>(sections.keySet()));
  }

  public ExtendedSizeEntry getSize(String file, String section) {
    final StringMap<SizeEntry> sections = lookUpMap.get(file);
    if (sections == null) {
      throw new IllegalArgumentException("Unknown file: " + file);
    }

    final SizeEntry sizeEntry = sections.get(section);
    if (sizeEntry == null) {
      throw new IllegalArgumentException("Unknown section '" + section + "' in file: " + file);
    }

    if (parserRegistry == null) {
      // Load parserRegistry
      parserRegistry = new ToolParserRegistry();
    }

    final ToolParser parser = parserRegistry.getParserById(sizeEntry.getParserId());
    if (parser == null) {
      throw new IllegalArgumentException("Unknown ToolParser in report for '" + section + "' in file: " + file);
    }

    return new ExtendedSizeEntry(sizeEntry, parser);
  }

  public void addEntries(Collection<SizeEntry> entries) {
    this.entries.addAll(entries);
    updateLookUpMap();
  }

  private void updateLookUpMap() {
    if (lookUpMap == null) {
      lookUpMap = new StringMap<StringMap<SizeEntry>>();
    }
    if (parserRegistry == null) {
      // Load parserRegistry
      parserRegistry = new ToolParserRegistry();
    }

    Set<SizeEntry> removals = new HashSet<SizeEntry>();

    for (Iterator<SizeEntry> i = this.entries.iterator(); i.hasNext(); ) {
      SizeEntry e = i.next();
      if (e.getParserId() == null
          || e.getSize() == null
          || StringUtils.isBlank(e.getFileName())
          || StringUtils.isBlank(e.getSectionName())) {
        i.remove();
        continue;
      }

      final ToolParser parser = parserRegistry.getParserById(e.getParserId());
      if (parser == null) {
        logger.severe("Unknown ToolParser mentioned in SizeEntry.");
        i.remove();
        continue;
      }

      final StringMap<SizeEntry> sectionMap = lookUpMap.getOrAdd(e.getFileName(), new StringMap<SizeEntry>());
      final SizeEntry existingEntry = sectionMap.get(e.getSectionName());
      if (existingEntry == null) {
        sectionMap.put(e.getSectionName(), e);
      } else if (e.getSize().asLong() > existingEntry.getSize().asLong()) {
        // New entry has larger size
        // - Remove old one
        removals.add(existingEntry);
        sectionMap.put(e.getSectionName(), e);
      } else {
        // Existing entry is larger do not add.
        i.remove();
      }
    }
  }

  private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    updateLookUpMap();
  }

  static class StringMap<T> extends HashMap<String, T> {
    T getOrAdd(String key, T defaultVal) {
      T obj = super.get(key);
      if (obj == null) {
        obj = defaultVal;
        super.put(key, defaultVal);
      }
      return obj;
    }
  }
}
