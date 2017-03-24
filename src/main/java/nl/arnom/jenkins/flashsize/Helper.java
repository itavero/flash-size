package nl.arnom.jenkins.flashsize;

import hudson.model.Run;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.logging.Logger;

/**
 * This class contains static methods with some utility/helper functions used by several classes,
 * as a quick way to reduce duplicated code.
 */
public abstract class Helper {

	private transient static final Logger logger = Logger.getLogger(Helper.class.getName());

	/**
	 * URL used for this plug-in.
	 */
	public static final String URL_NAME = "flash-size";

	/**
	 * @see #printLocation(String)
	 */
	public static void printLocation() {
		printLocation(null);
	}

	/**
	 * Log the location you are calling this method from.
	 * Useful for debugging purposes.
	 *
	 * @param msg Optional message.
	 */
	public static void printLocation(String msg) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		final int index = 3;
		if (stackTraceElements.length > index) {
			StringBuilder builder = new StringBuilder();
			final String file = stackTraceElements[index].getFileName();
			if (file == null) {
				builder.append("UNKNOWN");
			} else {
				builder.append(file);
			}
			builder.append(':');
			builder.append(stackTraceElements[index].getLineNumber());

			final String method = stackTraceElements[index].getMethodName();
			if (method != null) {
				builder.append("\t\t(");
				builder.append(method);
				builder.append(')');
			}
			if (StringUtils.isNotBlank(msg)) {
				builder.append("\t\t");
				builder.append(msg);
			}
			logger.fine(builder.toString());
		}
	}

	/**
	 * Checks if the given build has a report attached to it.
	 * If not, it requests the previous successful build, to check the same.
	 *
	 * @param start Starting Point
	 * @return BuildAction referencing the report and build of whichever build is a match.
	 */
	public static @Nullable BuildAction findBuildWithReport(Run<?,?> start) {
		if (start == null) {
			return null;
		}

		BuildAction action = getBuildActionWithReport(start);
		if (action != null) {
			return action;
		}

		Run<?,?> previousBuild = start.getPreviousSuccessfulBuild();
		action = getBuildActionWithReport(previousBuild);
		if (action != null) {
			return action;
		}

		return null;
	}

	public static @Nullable BuildAction getBuildActionWithReport(Run<?,?> build) {
		if (build == null) {
			return null;
		}

		final BuildAction action = build.getAction(BuildAction.class);
		if ((action != null) && (action.getReport() != null)) {
			return action;
		}
		return null;
	}

}
