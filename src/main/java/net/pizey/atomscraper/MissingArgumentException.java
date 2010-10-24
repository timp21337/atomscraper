/**
 * 
 */
package net.pizey.atomscraper;

import org.melati.util.MelatiRuntimeException;

/**
 * @author timp
 *
 */
public class MissingArgumentException extends MelatiRuntimeException {

  private static final long serialVersionUID = 5612700396502672123L;

  public MissingArgumentException() {
  }

  /**
   * @param message
   */
  public MissingArgumentException(String message) {
    super(message);
  }

  /**
   * @param subException
   */
  public MissingArgumentException(Exception subException) {
    super(subException);
  }

  /**
   * @param message
   * @param subException
   */
  public MissingArgumentException(String message, Exception subException) {
    super(message, subException);
  }

}
