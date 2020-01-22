
package io.github.mightguy.symspell.solr.utils;

/**
 * Use it for all Runtime Exceptions to pass check style.
 */
public class FatalException extends RuntimeException {

  public FatalException(String message) {
    super(message);
  }

  public FatalException(String message, Throwable cause) {
    super(message, cause);
  }

  public FatalException(Throwable cause) {
    super(cause);
  }

}
