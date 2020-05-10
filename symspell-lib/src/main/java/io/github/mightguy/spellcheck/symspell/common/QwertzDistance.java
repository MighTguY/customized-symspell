package io.github.mightguy.spellcheck.symspell.common;

import io.github.mightguy.spellcheck.symspell.api.CharDistance;
import java.util.HashMap;
import java.util.Map;

public class QwertzDistance implements CharDistance {

  double directConnect = 0.1f;
  double diagonalConnect = 0.4f;
  double defaultValue = 1f;

  Map<String, Double> operationCost = new HashMap<>();

  public QwertzDistance() {
    this.initializeCostMatrix();
  }

  @Override
  public double distance(char a, char b) {
    if (a == b) {
      return 0;
    }
    return operationCost
        .getOrDefault(a + "-" + b, operationCost.getOrDefault(b + "-" + a, defaultValue));
  }

  /**
   * Initializing the cost matrix
   */
  public void initializeCostMatrix() {

    //Middle row
    addReplaceWeight('a', "s", directConnect);
    addReplaceWeight('a', "qwy", diagonalConnect);

    addReplaceWeight('s', "ad", directConnect);
    addReplaceWeight('s', "wexy", diagonalConnect);

    addReplaceWeight('d', "sf", directConnect);
    addReplaceWeight('d', "erxc", diagonalConnect);

    addReplaceWeight('f', "sf", directConnect);
    addReplaceWeight('f', "erxc", diagonalConnect);

    addReplaceWeight('g', "fh", directConnect);
    addReplaceWeight('g', "tzvb", diagonalConnect);

    addReplaceWeight('h', "gj", directConnect);
    addReplaceWeight('h', "zubn", diagonalConnect);

    addReplaceWeight('j', "hk", directConnect);
    addReplaceWeight('j', "uinm", diagonalConnect);

    addReplaceWeight('k', "jl", directConnect);
    addReplaceWeight('k', "iom", diagonalConnect);

    addReplaceWeight('l', "kö", directConnect);
    addReplaceWeight('l', "op", diagonalConnect);

    addReplaceWeight('ö', "lä", directConnect);
    addReplaceWeight('ö', "pü", diagonalConnect);

    addReplaceWeight('ä', "ö", directConnect);
    addReplaceWeight('ä', "ü", diagonalConnect);

    //Top Row

    addReplaceWeight('q', "w", directConnect);
    addReplaceWeight('q', "a", diagonalConnect);

    addReplaceWeight('w', "qe", directConnect);
    addReplaceWeight('w', "as", diagonalConnect);

    addReplaceWeight('e', "wr", directConnect);
    addReplaceWeight('e', "sd", diagonalConnect);

    addReplaceWeight('r', "et", directConnect);
    addReplaceWeight('r', "df", diagonalConnect);

    addReplaceWeight('t', "rz", directConnect);
    addReplaceWeight('t', "fg", diagonalConnect);

    addReplaceWeight('z', "tu", directConnect);
    addReplaceWeight('z', "gh", diagonalConnect);

    addReplaceWeight('u', "zi", directConnect);
    addReplaceWeight('u', "hj", diagonalConnect);

    addReplaceWeight('i', "uo", directConnect);
    addReplaceWeight('i', "jk", diagonalConnect);

    addReplaceWeight('o', "ip", directConnect);
    addReplaceWeight('o', "kl", diagonalConnect);

    addReplaceWeight('p', "oü", directConnect);
    addReplaceWeight('p', "lö", diagonalConnect);

    addReplaceWeight('ü', "p", directConnect);
    addReplaceWeight('ü', "öä", diagonalConnect);

    //Bottom Row

    addReplaceWeight('y', "x", directConnect);
    addReplaceWeight('y', "sa", diagonalConnect);

    addReplaceWeight('x', "yc", directConnect);
    addReplaceWeight('x', "sd", diagonalConnect);

    addReplaceWeight('c', "xv", directConnect);
    addReplaceWeight('c', "df", diagonalConnect);

    addReplaceWeight('v', "bc", directConnect);
    addReplaceWeight('v', "fg", diagonalConnect);

    addReplaceWeight('b', "vn", directConnect);
    addReplaceWeight('b', "gh", diagonalConnect);

    addReplaceWeight('n', "bm", directConnect);
    addReplaceWeight('n', "hj", diagonalConnect);

    addReplaceWeight('m', "n", directConnect);
    addReplaceWeight('m', "jk", diagonalConnect);

  }

  /**
   *
   * @param a
   * @param listOfChars
   * @param connectWeight
   */
  private void addReplaceWeight(char a, String listOfChars, double connectWeight) {
    for (char ch : listOfChars.toCharArray()) {
      operationCost.put(a + "-" + ch, connectWeight);
    }
  }

  public void addReplaceWeight(char a, char b, double connectWeight) {
    operationCost.put(a + "-" + b, connectWeight);
  }
}
