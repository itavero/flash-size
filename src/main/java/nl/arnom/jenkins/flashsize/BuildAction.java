package nl.arnom.jenkins.flashsize;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import org.kohsuke.stapler.StaplerProxy;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BuildAction implements Action, StaplerProxy, Serializable, ViewInfoSource {

  private transient static final Logger logger = Logger.getLogger(BuildAction.class.getName());
  private final AbstractBuild<?, ?> build;
  private final FlashSizeReport report;
  private transient WeakReference<ResultsDisplay> resultsDisplay;
  private transient WeakReference<FlashSizeReport> previousReport;

  public BuildAction(AbstractBuild<?, ?> build, FlashSizeReport report) {
    this.build = build;
    this.report = report;
  }

  public String getIconFileName() {
    return "graph.png";
  }

  public String getDisplayName() {
    return "Flash Size";
  }

  public String getUrlName() {
    return Helper.URL_NAME;
  }

  public ResultsDisplay getTarget() {
    return getResultsDisplay();
  }

  private ResultsDisplay getResultsDisplay() {
    ResultsDisplay display = null;
    WeakReference<ResultsDisplay> wr = this.resultsDisplay;
    if (wr != null) {
      display = wr.get();
      if (display != null) {
        return display;
      }
    }

    try {
      display = new ResultsDisplay(this);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error creating new ResultsDisplay()", e);
    }
    this.resultsDisplay = new WeakReference<ResultsDisplay>(display);
    return display;
  }

  public FlashSizeReport getReport() {
    if (report == null) {
      final String buildId = this.getBuild().getId();
      logger.severe("BuildAction.getReport but report is null. Build: " + buildId);
    }

    return report;
  }

  public AbstractBuild<?, ?> getBuild() {
    return build;
  }

  public FlashSizeReport getPreviousReport() {
    if ((this.previousReport != null) && (this.previousReport.get() != null)) {
      return this.previousReport.get();
    }

    FlashSizeReport report = null;
    BuildAction previousBuildAction = Helper.findBuildWithReport(build.getPreviousBuild());
    if (previousBuildAction != null) {
      report = previousBuildAction.getReport();
    }

    if (report == null) {
      return null;
    }

    this.previousReport = new WeakReference<FlashSizeReport>(report);
    return report;
  }

  public boolean hasPreviousReport() {
    return (getPreviousReport() != null);
  }

  public FileSize getDeltaFromPreviousReport(final String file) {
    if (!hasPreviousReport()) {
      return null;
    }

    FileSize result = null;
    try {
      final FileSize now = this.getReport().getTotalSizeOfFile(file);
      final FileSize then = this.getPreviousReport().getTotalSizeOfFile(file);
      result = new FileSize(now.asLong() - then.asLong());
    } catch (RuntimeException e) {
      // Failed to determine delta
    }
    return result;
  }

  public FileSize getDeltaFromPreviousReport(final SizeEntry latest) {
    if ((!hasPreviousReport()) || (latest == null)) {
      return null;
    }

    FileSize result = null;
    try {
      final Long now = latest.getSize().asLong();
      final Long then = this.getPreviousReport().getSize(latest.getFileName(), latest.getSectionName()).getSize().asLong();
      result = new FileSize(now - then);
    } catch (RuntimeException e) {
      // Failed to determine delta
    }
    return result;
  }
}
