
package com.lsharma.spellcheck.symspell.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * SpellCheckSetting contians all the setting used by
 * {@link com.lsharma.spellcheck.symspell.api.SpellChecker}
 */
@ToString
@Builder
@Getter
public class SpellCheckSettings {


  /**
   * Default verbosity {@link Verbosity}
   */
  @Builder.Default
  private Verbosity verbosity = Verbosity.ALL;

  /**
   * limit suggestion list to topK entries
   */
  @Builder.Default
  private int topK = 10; // limits result to n entries


  /**
   * Damerau function variables Deletion weight: 1.20 ~ 1.40
   */
  @Builder.Default
  private float deletionWeight = 1.0f;

  /**
   * Damerau function variables Insertion weight: 1.01
   */
  @Builder.Default
  private float insertionWeight = 1.0f;

  /**
   * Damerau function variables Replace weight: 0.9f ~ 1.20
   */
  @Builder.Default
  private float replaceWeight = 1.0f;
  /**
   * Damerau function variables Transposition weight: 0.7f ~ 1.05
   */
  @Builder.Default
  private float transpositionWeight = 1.0f;


  /**
   * true if the spellchecker should lowercase terms
   */
  @Builder.Default
  private boolean lowerCaseTerms = true;


  /**
   * Maximum edit distance for doing lookups. (default 2.0)
   */
  @Builder.Default
  private double maxEditDistance = 2;

  /**
   * The length of word prefixes used for spell checking. (default 7)
   */
  @Builder.Default
  private int prefixLength = 7;

  /**
   * The minimum frequency count for dictionary words to be considered correct spellings. (default
   * 1)
   */
  @Builder.Default
  private long countThreshold = 1;

  /**
   * Max keywordLength;
   */
  @Setter
  @Builder.Default
  private int maxLength = Integer.MAX_VALUE;

  @Setter
  @Builder.Default
  private double bigramCountMin = Double.MAX_VALUE;

}
