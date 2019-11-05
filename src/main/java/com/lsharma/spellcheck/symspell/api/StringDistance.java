
package com.lsharma.spellcheck.symspell.api;

public interface StringDistance {

  /**
   * Get edit distance between 2 words;
   *
   * @param w1 String
   * @param w2 String
   * @return edit distance
   */
  double getDistance(String w1, String w2);

  /**
   * Get edit distance between 2 words, if the calculated distance exceeds max provided then return
   * maxEditDistance;
   *
   * @param w1 String
   * @param w2 String
   * @param maxEditDistance max edit distance possible
   * @return edit distance
   */
  double getDistance(String w1, String w2, double maxEditDistance);
}
