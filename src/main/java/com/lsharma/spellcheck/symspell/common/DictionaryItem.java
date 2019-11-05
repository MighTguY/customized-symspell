
package com.lsharma.spellcheck.symspell.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Dictionary Item class which holds the term, frequency and the edit distance
 */
@AllArgsConstructor
@Getter
public class DictionaryItem {

  String term;
  Double frequency;
  Double distance;
}
