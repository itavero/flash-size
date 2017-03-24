package nl.arnom.jenkins.flashsize.parsers;

import hudson.model.TaskListener;
import hudson.util.LogTaskListener;
import nl.arnom.jenkins.flashsize.SizeEntry;
import nl.arnom.jenkins.flashsize.ToolParser;
import org.junit.Before;
import org.junit.Test;

import java.io.Reader;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class IarXlinkParserTest extends ToolParserTest {

  private transient static final Logger logger = Logger.getLogger(ToolParserTest.class.getName());
  private ToolParser parser;
  ;
  private TaskListener listener;

  @Before
  public void setup() {
    parser = new IarXlinkParser();
    listener = new LogTaskListener(logger, Level.SEVERE);
  }

  @Test
  public void mayNotReturnNull() throws Exception {
    // Arrange
    // not applicable

    // Act
    final Collection<SizeEntry> result = parser.parse(listener, null, true);

    // Assert
    assertNotNull(result);
  }

  @Test
  public void parse() throws Exception {
    // Arrange
    final Reader testReader = openFile("IarXlinkLog.txt");

    // Act
    final Collection<SizeEntry> result = parser.parse(listener, testReader, true);

    // Assert
    assertNotNull(result);
    assertEquals(10, result.size());

    // TODO Improve tests.
  }
}
