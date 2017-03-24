package nl.arnom.jenkins.flashsize.parsers;

import org.apache.commons.io.input.BOMInputStream;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

/**
 * Created by arnom on 30/03/2017.
 */
public abstract class ToolParserTest {

  protected Reader openFile(final String fileName) {
    try {
      return new InputStreamReader(new BOMInputStream(ToolParserTest.class.getResourceAsStream(fileName)), "UTF-8");
    } catch (UnsupportedEncodingException exception) {
      return new InputStreamReader(ToolParserTest.class.getResourceAsStream(fileName));
    }
  }
}
