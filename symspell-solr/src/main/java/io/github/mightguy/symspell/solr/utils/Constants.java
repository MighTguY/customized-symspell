package io.github.mightguy.symspell.solr.utils;

public final class Constants {

  private Constants() {

  }

  public static final String SPELLCHECK_PREFIX = "cspellcheck.";
  public static final String SPELLCHECK_Q = SPELLCHECK_PREFIX + "q";
  public static final String SPELLCHECK_DATALOAD_UNIGRAM = SPELLCHECK_PREFIX + "dataload.unigram";
  public static final String SPELLCHECK_DATALOAD_BIGRAM = SPELLCHECK_PREFIX + "dataload.bigram";
  public static final String SPELLCHECK_THRESHOLD = SPELLCHECK_PREFIX + "threshold";
  public static final String SPELLCHECK_ENABLE = SPELLCHECK_PREFIX + "enable";
  public static final String SPELLCHECK_BUILD = SPELLCHECK_PREFIX + "build";
}
