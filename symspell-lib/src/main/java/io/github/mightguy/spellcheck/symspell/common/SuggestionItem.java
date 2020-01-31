
package io.github.mightguy.spellcheck.symspell.common;

import java.util.Comparator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@Setter
public class SuggestionItem implements Comparator<SuggestionItem>, Comparable<SuggestionItem> {

  /**
   * The suggested correctly spelled word.
   */
  private String term;

  /**
   * Edit distance between searched for word and suggestion.
   */
  private double distance;

  /**
   * Frequency of suggestion in the dictionary (a measure of how common the word is).
   */
  private double count;

  /**
   * COnstructor  for SuggestionItem
   * @param term
   * @param distance
   * @param count
   */
  public SuggestionItem(String term, double distance, double count) {
    this.term = term;
    this.distance = distance;
    this.count = count;
  }

  /**
   * final similarity
   */
  private double score;

  /**
   * Comparison to use in Sorting: Prefernce given to distance, and if distance is same then count
   */
  @Override
  public int compareTo(SuggestionItem other) {
    if (SpellHelper.isEqualDouble(this.distance, other.distance, 0.001f)) {
      return Double.compare(other.count, this.count);
    }
    return Double.compare(this.distance, other.distance);
  }

  @Override
  public int compare(SuggestionItem suggestItem, SuggestionItem suggestItem2) {
    return suggestItem.compareTo(suggestItem2);
  }

}
