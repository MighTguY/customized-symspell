
package io.github.mightguy.spellcheck.symspell.common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;


public final class SpellHelper {

  private SpellHelper() {
  }

  /**
   * Generate delete string set  for the Key.
   */
  public static Set<String> getEditDeletes(String key,
      double maxEditDistance, int prefixLength) {
    Set<String> deletedWords = new HashSet<>();
    if (key.length() <= maxEditDistance) {
      deletedWords.add("");
    }
    if (key.length() > maxEditDistance) {
      deletedWords
          .add(key.substring(0,
              prefixLength < key.length() ? prefixLength : key.length()));
    }
    return edits(key, 0, deletedWords, getEdistance(maxEditDistance, key.length()));
  }

  private static Double getEdistance(double maxEditDistance, int length) {
    double factor = 0.3;
    double computedEd = Math.round(factor * length);
    if (Math.min(maxEditDistance, computedEd) == maxEditDistance) {
      return maxEditDistance;
    }
    return computedEd;
  }

  /**
   * Inexpensive and language independent: only deletes, no transposes + replaces + inserts replaces
   * and inserts are expensive and language dependent
   */
  public static Set<String> edits(String word, double editDistance, Set<String> deletedWords,
      final Double maxEd) {
    editDistance++;
    if (word.length() < 1) {
      return deletedWords;
    }

    for (int i = 0; i < word.length(); i++) {
      String delete = word.substring(0, i) + word.substring(i + 1, word.length());
      if (deletedWords.add(delete) && editDistance < maxEd) {
        edits(delete, editDistance, deletedWords, maxEd);
      }
    }
    return deletedWords;
  }

  /**
   * Early exit method
   */
  public static List<SuggestionItem> earlyExit(List<SuggestionItem> suggestionItems,
      String phrase, double maxEditDistance) {
    if (CollectionUtils.isNotEmpty(suggestionItems)) {
      suggestionItems.add(new SuggestionItem(phrase, maxEditDistance + 1, 0));
    }
    return suggestionItems;
  }

  public static String[] tokenizeOnWhiteSpace(String word) {
    return word.split("\\s+");
  }

  public static boolean isLessOrEqualDouble(double d1, double d2, final double threshold) {
    return Math.abs(d1 - d2) < threshold || d1 < d2;
  }

  public static boolean isLessDouble(double d1, double d2, final double threshold) {
    return !isEqualDouble(d1, d2, threshold) && d1 < d2;
  }


  public static boolean isEqualDouble(double d1, double d2, final double threshold) {
    return Math.abs(d1 - d2) < threshold;
  }

  public static boolean continueConditionIfHeadIsSame(List<SuggestionItem> suggestions,
      List<SuggestionItem> suggestions1) {
    return CollectionUtils.isEmpty(suggestions1)
        || (CollectionUtils.isNotEmpty(suggestions)
        && CollectionUtils.isNotEmpty(suggestions1)
        && suggestions.get(0).equals(suggestions1.get(0)));
  }

}
