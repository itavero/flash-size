package nl.arnom.jenkins.flashsize;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;

import java.io.IOException;

/**
 * Created by amoonen on 27-3-2017.
 */
public class ResultsDisplay implements ModelObject, ViewInfoSource {

  private static AbstractBuild<?, ?> currentBuild = null;
  private transient BuildAction buildAction;
  private FlashSizeReport currentReport;

  ResultsDisplay(final BuildAction buildAction)
      throws IOException {
    this.buildAction = buildAction;

    currentReport = this.buildAction.getReport();
  }

  public String getDisplayName() {
    return "Flash Size";
  }

  public AbstractBuild<?, ?> getBuild() {
    return buildAction.getBuild();
  }

  public FlashSizeReport getReport() {
    return buildAction.getReport();
  }

  public boolean hasPreviousReport() {
    return buildAction.hasPreviousReport();
  }

  public FileSize getDeltaFromPreviousReport(final String file) {
    return buildAction.getDeltaFromPreviousReport(file);
  }

  public FileSize getDeltaFromPreviousReport(final SizeEntry latest) {
    return buildAction.getDeltaFromPreviousReport(latest);
  }
}
