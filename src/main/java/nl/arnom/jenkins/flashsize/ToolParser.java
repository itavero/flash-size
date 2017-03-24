package nl.arnom.jenkins.flashsize;

import hudson.ExtensionPoint;
import hudson.model.TaskListener;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.Collection;

/**
 * Interface for a class that can parse the output of a specific tool.
 *
 * @author Arno Moonen
 */
public interface ToolParser extends ExtensionPoint, Serializable {

  /**
   * @return Human readable name for the parser
   */
  String getToolName();

  /**
   * This identifier is stored in the configuration files.
   * It must be unique, so you might want to use the class name or something
   * like that.
   *
   * @return Unique Identifier
   */
  String getUniqueIdentifier();

  /**
   * Parse the given input for flash size information.
   *
   * @param reader    Input data (can be a file or the build log)
   * @param isConsole True if the data in `reader` is from the console. False if it is a file.
   *
   * @return Set of size entries
   *
   * @throws IOException In case of an error with the reader.
   */
  Collection<SizeEntry> parse(TaskListener listener, final Reader reader, boolean isConsole) throws IOException;

}
