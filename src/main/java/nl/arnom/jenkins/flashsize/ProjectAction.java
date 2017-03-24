package nl.arnom.jenkins.flashsize;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Run;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.logging.Logger;

/**
 * Created by amoonen on 27-3-2017.
 */
public class ProjectAction implements Action, ViewInfoSource {
  private static final Logger LOGGER = Logger.getLogger(ProjectAction.class.getName());

  private final AbstractProject<?, ?> project;
  private transient WeakReference<Run<?,?>> lastFinishedBuild;
  private transient WeakReference<BuildAction> latestBuildAction;
  private transient WeakReference<Integer> lastResultBuild;
  private transient static final Logger logger = Logger.getLogger(BuildAction.class.getName());

  public ProjectAction(AbstractProject<?, ?> project) {
    this.project = project;
  }

  public AbstractProject<?, ?> getProject() {
    return project;
  }

  public boolean showSummary() {
    updateLastFinishedBuild();
    return (getLatestBuildAction() != null && getLastResultBuild() != null);
  }

  public FlashSizeReport getReport() {
    return getLatestBuildAction().getReport();
  }

  public boolean hasPreviousReport() {
    return getLatestBuildAction().hasPreviousReport();
  }

  public FileSize getDeltaFromPreviousReport(String file) {
    return getLatestBuildAction().getDeltaFromPreviousReport(file);
  }

  public FileSize getDeltaFromPreviousReport(SizeEntry latest) {
    return getLatestBuildAction().getDeltaFromPreviousReport(latest);
  }

  private void updateLastFinishedBuild() {
    Run<?, ?> build  = project.getLastBuild();
    while (build != null && build.isBuilding()) {
      build = build.getPreviousBuild();
    }

    boolean storeBuild = false;
    if (lastFinishedBuild != null) {
      if (build != null) {
        storeBuild = !build.equals(lastFinishedBuild.get());
      } else {
        storeBuild = lastFinishedBuild.get() != null;
      }
    } else {
      storeBuild = true;
    }

    if (storeBuild) {
      lastFinishedBuild = new WeakReference<Run<?, ?>>(build);
      latestBuildAction = null;
      lastResultBuild = null;
    }
  }

  public @Nullable BuildAction getLatestBuildAction() {
    BuildAction action = null;
    WeakReference<BuildAction> wr = this.latestBuildAction;
    if (wr != null) {
      action = wr.get();
      if (action != null) {
        return action;
      }
    }

    try {
      if (lastFinishedBuild == null || lastFinishedBuild.get() == null) {
        return null;
      }
      action = lastFinishedBuild.get().getAction(BuildAction.class);
      if (action.getReport() == null) {
        // ignore this one
        action = null;
      } else {
        this.latestBuildAction = new WeakReference<BuildAction>(action);
      }

    } catch (NullPointerException e) {
      // ignore
    }

    return action;
  }

  @Override
  public String getIconFileName() {
    return "graph.png";
  }

  @Override
  public String getDisplayName() {
    return "Flash Size";
  }

  @Override
  public String getUrlName() {
    return Helper.URL_NAME;
  }

  public @Nullable Integer getLastResultBuild() {
    Integer number = null;
    WeakReference<Integer> wr = this.lastResultBuild;
    if (wr != null) {
      number = wr.get();
      if (number != null) {
        return number;
      }
    }

    try {
      number = latestBuildAction.get().getBuild().getNumber();
    }
    catch (NullPointerException ex) {
      // ignore, we have a fallback below
    }

    if (number == null) {
      if (lastFinishedBuild != null || lastFinishedBuild.get() != null) {
        BuildAction action = Helper.findBuildWithReport(lastFinishedBuild.get());
        if (action != null && action.getBuild() != null) {
          number = action.getBuild().getNumber();
        }
      }
    }

    if (number != null) {
      lastResultBuild = new WeakReference<Integer>(number);
    }
    return number;
  }

  public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
    Integer buildNumber = getLastResultBuild();
    if (buildNumber == null) {
      rsp.sendRedirect2("..");
    } else {
      rsp.sendRedirect2("../" + buildNumber + "/" + getUrlName());
    }
  }
}
