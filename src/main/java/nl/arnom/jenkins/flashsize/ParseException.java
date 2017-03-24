package nl.arnom.jenkins.flashsize;

import java.io.IOException;

/**
 * Created by amoonen on 27-3-2017.
 */
public class ParseException extends IOException {

  public ParseException() {
    super();
  }

  public ParseException(String message) {
    super(message);
  }

  public ParseException(String message, Throwable cause) {
    super(message, cause);
  }

  public ParseException(Throwable cause) {
    super(cause);
  }
}
