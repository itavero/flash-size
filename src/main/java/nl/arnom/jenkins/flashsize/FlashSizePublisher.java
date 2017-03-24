package nl.arnom.jenkins.flashsize;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.util.ListBoxModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.*;

public class FlashSizePublisher extends Publisher {

  private final String parser;
  private final String includeFiles;
  private final String excludeFiles;
  private final Boolean scanConsole;
  private final Collection<String> excludedSections;
  private final Collection<String> includedSections;

  @DataBoundConstructor
  public FlashSizePublisher(String parser, String includeFiles, String excludeFiles, Boolean scanConsole, String includedSections, String excludedSections) {
    this.parser = parser;
    this.includeFiles = includeFiles;
    this.excludeFiles = excludeFiles;
    this.scanConsole = scanConsole;
    this.includedSections = parseSectionPatterns(includedSections);
    this.excludedSections = parseSectionPatterns(excludedSections);
  }

  private static @Nonnull Collection<String> parseSectionPatterns(String sections) {
    List<String> sectionList = new ArrayList<String>(StringUtils.countMatches(sections, ",") + 1);
    String[] initialParse = StringUtils.stripAll(StringUtils.split(sections, ','));
    for (String s : initialParse) {
      if (StringUtils.isEmpty(s) || StringUtils.containsOnly(s, "*")) {
        continue;
      }
      final int firstStar = s.indexOf('*');
      if ((firstStar >= 0)
          && (firstStar != s.lastIndexOf('*'))) {
        // Can have only one star
        continue;
      }
      if ((firstStar < 0)
          || (firstStar == 0)
          || (firstStar == (s.length() - 1))) {
        // Star must be first or last character, or not present
        final String lowercase = s.toLowerCase();
        if (!sectionList.contains(lowercase)) {
          sectionList.add(lowercase);
        }
      }
    }
    return sectionList;
  }

  public String getParser() {
    return parser;
  }

  public String getIncludeFiles() {
    return includeFiles;
  }

  public String getExcludeFiles() {
    return excludeFiles;
  }

  public Boolean getScanConsole() {
    return scanConsole;
  }

  public String getIncludedSections() {
    return StringUtils.join(includedSections, ", ");
  }

  public void setIncludedSections(String sections) {
    includedSections.clear();
    includedSections.addAll(parseSectionPatterns(sections));
  }

  public String getExcludedSections() {
    return StringUtils.join(excludedSections, ", ");
  }

  public void setExcludedSections(String sections) {
    excludedSections.clear();
    excludedSections.addAll(parseSectionPatterns(sections));
  }

  @Override
  public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
    try {
      final ToolParserRegistry registry = new ToolParserRegistry();
      final ToolParser toolParser = registry.getParserById(parser);
      if (toolParser == null) {
        if (parser == null) {
          throw new ParseException("No parser configured.");
        }
        throw new ParseException("Parser with ID '" + parser + "' not found.");
      }

      final FlashSizeReport report = new FlashSizeReport(includedSections, excludedSections);
      final Collection<SizeEntry> fileEntries = doScanFiles(build, listener, toolParser);
      if (CollectionUtils.isNotEmpty(fileEntries)) {
        report.addEntries(fileEntries);
      }

      if (scanConsole) {
        final Collection<SizeEntry> consoleEntries = doScanConsole(build, listener, toolParser);
        if (CollectionUtils.isNotEmpty(consoleEntries)) {
          report.addEntries(consoleEntries);
        }
      }

      // Publish result
      final BuildAction buildAction = new BuildAction(build, report);
      build.addAction(buildAction);
    } catch (Throwable t) {
      // For now (while still in development) catch all exceptions to prevent the build from failing due to a coding error.
      final PrintStream logger = listener.getLogger();
      logger.format("Exception %s thrown in Flash Size Publisher: %s%n", t.getClass().getSimpleName(), t.getMessage());
      for (StackTraceElement stackTraceElement : t.getStackTrace()) {
        logger.println(stackTraceElement.toString());
      }
    }
    return true;
  }

  @Override
  public Action getProjectAction(AbstractProject<?, ?> project) {
    return new ProjectAction(project);
  }

  private @Nonnull
  Collection<SizeEntry> doScanConsole(AbstractBuild<?, ?> build, BuildListener listener, ToolParser toolParser) throws IOException {
    listener.getLogger().format("Scanning build log for flash size information with %s parser...%n", toolParser.getToolName());
    final Reader reader = build.getLogReader();
    return toolParser.parse(listener, reader, true);
  }

  private @Nonnull
  Collection<SizeEntry> doScanFiles(AbstractBuild<?, ?> build, BuildListener listener, ToolParser toolParser) {
    final Set<String> includes = parsePatterns(includeFiles);
    final Set<SizeEntry> entries = new HashSet<SizeEntry>();
    if (includes.isEmpty()) {
      // No include patterns, thus not scanning files.
      return entries;
    }

    final Set<String> excludes = parsePatterns(excludeFiles);
    final FileScanner scanFiles = new FileScanner(listener, toolParser, includes, excludes);
    try {
      entries.addAll(build.getWorkspace().act(scanFiles));
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    return entries;
  }

  private @Nonnull Set<String> parsePatterns(String input) {
    final Set<String> patterns = new HashSet<String>();
    CollectionUtils.addAll(patterns, StringUtils.stripAll(StringUtils.split(input, ',')));
    return patterns;
  }

  public BuildStepMonitor getRequiredMonitorService() {
    return BuildStepMonitor.NONE;
  }

  @Extension
  public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
      return true;
    }

    @Override
    public String getDisplayName() {
      return "Record flash size";
    }

    public ListBoxModel doFillParserItems() {
      return ToolParserRegistry.getParsersAsListModel();
    }
  }
}
