package nl.arnom.jenkins.flashsize;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import jenkins.security.Roles;
import org.apache.commons.collections.CollectionUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.jenkinsci.remoting.RoleChecker;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FileScanner implements FilePath.FileCallable<Collection<SizeEntry>> {
  private final ToolParser toolParser;
  private final String[] includes;
  private final String[] excludes;
  private final TaskListener listener;

  public FileScanner(TaskListener listener, ToolParser toolParser, Collection<String> includes, Collection<String> excludes) {
    this(listener, toolParser, includes.toArray(new String[includes.size()]), excludes.toArray(new String[excludes.size()]));
  }

  public FileScanner(TaskListener listener, ToolParser toolParser, String[] includes, String[] excludes) {
    this.listener = listener;
    this.toolParser = toolParser;
    this.includes = includes;
    this.excludes = excludes;
  }

  @Override
  public Collection<SizeEntry> invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
    final DirectoryScanner scanner = new DirectoryScanner();
    scanner.setFollowSymlinks(false);
    scanner.setBasedir(f);
    scanner.setIncludes(includes);
    scanner.setExcludes(excludes);
    scanner.scan();

    final Set<SizeEntry> entries = new HashSet<SizeEntry>();
    if (scanner.getIncludedFilesCount() == 0) {
      listener.getLogger().format("No files found to parse with %s parser.%n", toolParser.getToolName());
      return entries;
    }

    listener.getLogger().format("Scanning %d files for flash size information with %s parser:%n", scanner.getIncludedFilesCount(), toolParser.getToolName());
    for (String file : scanner.getIncludedFiles()) {
      final FilePath path = new FilePath(new File(f, file));
      final Reader reader = new InputStreamReader(path.read());
      final Collection<SizeEntry> found = toolParser.parse(listener, reader, false);
      reader.close();
      if (CollectionUtils.isNotEmpty(found)) {
        listener.getLogger().format("Found %d entries in file '%s'.%n", found.size(), file);
        entries.addAll(found);
      } else {
        listener.getLogger().format("No entries in file '%s'.%n", file);
      }
    }
    return entries;
  }

  @Override
  public void checkRoles(RoleChecker checker) throws SecurityException {
    checker.check(this, Roles.SLAVE);
  }
}
