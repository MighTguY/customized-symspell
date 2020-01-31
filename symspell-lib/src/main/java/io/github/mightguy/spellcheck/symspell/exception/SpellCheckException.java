
package io.github.mightguy.spellcheck.symspell.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class SpellCheckException extends Exception {

  private final SpellCheckExceptionCode spellCheckExceptionCode;
  private final String customMessage;

  /**
   * Exception for SPellcheck
   * @param code
   */
  public SpellCheckException(SpellCheckExceptionCode code) {
    super();
    this.spellCheckExceptionCode = code;
    this.customMessage = "";
  }

  /**
   * Exception for SPellcheck
   * @param code
   * @param message
   */
  public SpellCheckException(SpellCheckExceptionCode code, String message) {
    super(message);
    this.spellCheckExceptionCode = code;
    this.customMessage = message;
  }

  /**
   * Exception for SPellcheck
   * @param message
   * @param cause
   * @param code
   */
  public SpellCheckException(String message, Throwable cause, SpellCheckExceptionCode code) {
    super(message, cause);
    this.spellCheckExceptionCode = code;
    this.customMessage = message;
  }

  /**
   * Exception for SPellcheck
   * @param cause
   * @param code
   */
  public SpellCheckException(Throwable cause, SpellCheckExceptionCode code) {
    super(cause);
    this.spellCheckExceptionCode = code;
    this.customMessage = cause.getMessage();
  }

  /**
   * Exception for SPellcheck
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   * @param code
   */
  protected SpellCheckException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace, SpellCheckExceptionCode code) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.spellCheckExceptionCode = code;
    this.customMessage = message;
  }
}
