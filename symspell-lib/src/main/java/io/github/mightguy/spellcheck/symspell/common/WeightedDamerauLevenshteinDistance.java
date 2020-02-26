
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
  public double getDistance(String a, String b) {
    boolean useCharDistance = (charDistance != null && a.length() == b.length());
    double[][] d = new double[a.length() + 1][b.length() + 1]; // 2d matrix

    // Step 1
    if (a.length() == 0) {
      return b.length();
    }
    if (b.length() == 0) {
      return a.length();
    }

    // Step 2
    for (int i = a.length(); i >= 0; i--) {
      d[i][0] = i;  // Add deletion weight
    }
    for (int j = b.length(); j >= 0; j--) {
      d[0][j] = j;
    }

    for (int i = 1; i <= a.length(); i++) {
      char aI = a.charAt(i - 1);

      for (int j = 1; j <= b.length(); j++) {
        char bJ = b.charAt(j - 1);

        double cost = getReplaceCost(aI, bJ, useCharDistance);

        double min = Math
            .min(d[i - 1][j] + deletionWeight,
                Math.min(d[i][j - 1] + insertionWeight,
                    d[i - 1][j - 1] + cost));

        if (isTransposition(i, j, a, b)) {
          min = Math.min(min, d[i - 2][j - 2] + transpositionWeight);
        }

        if ((min == d[i - 1][j - 1] + cost) && useCharDistance && aI != bJ) {
          useCharDistance = false;
        }

        d[i][j] = min;
      }
    }

    // Step 5
    return d[a.length()][b.length()];
  }

  @Override
  public double getDistance(String w1, String w2, double maxEditDistance) {

    double distance = getDistance(w1, w2);
    if (distance > maxEditDistance) {
      return -1;
    }
    return distance;
  }

  private boolean isTransposition(int i, int j, String a, String b) {
    return i > 2
        && j > 2
        && a.charAt(i - 2) == b.charAt(i - 1)
        && b.charAt(j - 2) == a.charAt(j - 1);
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
