
package io.github.mightguy.spellcheck.symspell.common;

import io.github.mightguy.spellcheck.symspell.api.CharDistance;
import io.github.mightguy.spellcheck.symspell.api.StringDistance;

/**
 * DamerauLevenshteinDistance is a string metric for measuring the edit distance between two
 * sequences. Informally, the Damerau–Levenshtein distance between two words is the minimum number
 * of operations (consisting of insertions, deletions or substitutions of a single character, or
 * transposition of two adjacent characters) required to change one word into the other.
 *
 * In this variant of DamerauLevenshteinDistance, it has different weights associated to each
 * action.
 */
public class WeightedDamerauLevenshteinDistance implements StringDistance {

  // Damerau function variables
  private double deletionWeight = 0.8f;

  private double insertionWeight = 1.01f;

  private double replaceWeight = 0.9f;

  private double transpositionWeight = 0.7f;

  private CharDistance charDistance;

  /**
   * Constructor for Weighted Damerau Levenshtein
   * @param deletionWeight
   * @param insertionWeight
   * @param replaceWeight
   * @param transpositionWeight
   * @param charDistance
   */
  public WeightedDamerauLevenshteinDistance(double deletionWeight, double insertionWeight,
      double replaceWeight, double transpositionWeight, CharDistance charDistance) {
    this.deletionWeight = deletionWeight;
    this.insertionWeight = insertionWeight;
    this.replaceWeight = replaceWeight;
    this.transpositionWeight = transpositionWeight;
    this.charDistance = charDistance;
  }

  @Override
  public double getDistance(String source, String target) {

    if (source.equals(target)) {
      return 0;
    }

    if (source.length() == 0) {
      return target.length();
    }

    if (target.length() == 0) {
      return source.length();
    }

    boolean useCharDistance = (charDistance != null && source.length() == target.length());
    double[][] d = new double[target.length() + 1][source.length() + 1]; // 2d matrix

    // Step 2
    for (int i = target.length(); i >= 0; i--) {
      d[i][0] = i * insertionWeight;  // Add insertion weight
    }
    for (int j = source.length(); j >= 0; j--) {
      d[0][j] = j * deletionWeight;
    }

    for (int i = 1; i <= target.length(); i++) {
      char target_i = target.charAt(i - 1);
      for (int j = 1; j <= source.length(); j++) {
        char source_j = source.charAt(j - 1);

        double cost = getReplaceCost(target_i, source_j, useCharDistance);

        double min = min(d[i - 1][j] + insertionWeight, //Insertion
            d[i][j - 1] + deletionWeight, //Deltion
            d[i - 1][j - 1] + cost); //Replacement
        if (isTransposition(i, j, source, target)) {
          min = Math.min(min, d[i - 2][j - 2] + transpositionWeight); // transpose
        }
        d[i][j] = min;
      }
    }
    return d[target.length()][source.length()];
  }

  @Override
  public double getDistance(String w1, String w2, double maxEditDistance) {

    double distance = getDistance(w1, w2);
    if (distance > maxEditDistance) {
      return -1;
    }
    return distance;
  }

  private double min(double a, double b, double c) {
    return Math.min(a, Math.min(b, c));
  }

  private double min(double a, double b, double c, double d) {
    return Math.min(a, Math.min(b, Math.min(c, d)));
  }



  private boolean isTransposition(int i, int j, String source, String target) {
    return i > 2
        && j > 2
        && source.charAt(j - 2) == target.charAt(i - 1)
        && target.charAt(i - 2) == source.charAt(j - 1);
  }

  private double getReplaceCost(char aI, char bJ, boolean useCharDistance) {
    if (aI != bJ && useCharDistance) {
      return replaceWeight * charDistance.distance(aI, bJ);
    } else if (aI != bJ) {
      return replaceWeight;
    } else {
      return 0;
    }
  }
}
