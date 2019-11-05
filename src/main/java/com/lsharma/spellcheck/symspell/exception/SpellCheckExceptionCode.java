
package com.lsharma.spellcheck.symspell.exception;

public enum SpellCheckExceptionCode {

  LOOKUP_ERROR("Exception occured while looking up the term"),
  INDEX_ERROR("Exception occured while indexing  the term"),
  DELETE_ERROR("Exception occured while deleting up the term"),
  DUMP_ERROR("Exception occured while dumping up the term");

  private String message;

  SpellCheckExceptionCode(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
