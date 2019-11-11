
package io.github.mightguy.spellcheck.symspell;

import io.github.mightguy.spellcheck.symspell.api.StringDistance;
import io.github.mightguy.spellcheck.symspell.common.QwertyDistance;
import io.github.mightguy.spellcheck.symspell.common.WeightedDamerauLevenshteinDistance;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestedWDamerauLevenshtein {

  static StringDistance damerauLevenshtein;
  static StringDistance wdamerauLevenshtein;
  static StringDistance customDamerauLevenshtein;


  @BeforeClass
  public static void setup() {
    damerauLevenshtein = new WeightedDamerauLevenshteinDistance(1, 1, 1, 1, null);
    wdamerauLevenshtein = new WeightedDamerauLevenshteinDistance(0.8f, 1.01f, 0.9f, 0.7f, null);
    customDamerauLevenshtein = new WeightedDamerauLevenshteinDistance(0.8f, 1.01f, 0.9f, 0.7f,
        new QwertyDistance());
  }


  @Test
  public void testWeightedDamerauLevenshtein() {
    Assert.assertEquals(0.89, wdamerauLevenshtein.getDistance("sommer", "summer"), 0.01);
    Assert.assertEquals(0.89, wdamerauLevenshtein.getDistance("bigjest", "big est"), 0.01);
    Assert.assertEquals(1.8, wdamerauLevenshtein.getDistance("cool", "cola"), 0.01);
    Assert.assertEquals(0.9, wdamerauLevenshtein.getDistance("slives", "slices"), 0.01);
    Assert.assertEquals(0.9, wdamerauLevenshtein.getDistance("slives", "olives"), 0.01);
    Assert.assertEquals(0, wdamerauLevenshtein.getDistance("slime", "slime"), 0.01);
  }

  @Test
  public void testDamerauLevenshtein() {
    Assert.assertEquals(1, damerauLevenshtein.getDistance("sommer", "summer"), 0.01);
    Assert.assertEquals(1, damerauLevenshtein.getDistance("bigjest", "big est"), 0.01);
    Assert.assertEquals(2, damerauLevenshtein.getDistance("cool", "cola"), 0.01);
    Assert.assertEquals(1, damerauLevenshtein.getDistance("slives", "slices"), 0.01);
    Assert.assertEquals(1, damerauLevenshtein.getDistance("slives", "olives"), 0.01);
    Assert.assertEquals(0, damerauLevenshtein.getDistance("slime", "slime"), 0.01);
    Assert.assertEquals(1, damerauLevenshtein.getDistance("playrs", "players"), 0.01);
    Assert.assertEquals(2, damerauLevenshtein.getDistance("playrs", "player"), 0.01);
  }

  @Test
  public void testCustomDamerauLevenshtein() {
    Assert.assertEquals(0.899, customDamerauLevenshtein.getDistance("bigjest", "biggest"), 0.01);
    Assert.assertEquals(0.899, customDamerauLevenshtein.getDistance("bigjest", "big est"), 0.01);
    Assert.assertEquals(1.009, customDamerauLevenshtein.getDistance("bigjest", "big jest"), 0.01);
    Assert.assertEquals(1.25, customDamerauLevenshtein.getDistance("cool", "cola"), 0.01);
    Assert.assertEquals(0.08, customDamerauLevenshtein.getDistance("slives", "slices"), 0.01);
    Assert.assertEquals(0.899, customDamerauLevenshtein.getDistance("slives", "olives"), 0.01);
    Assert.assertEquals(0, customDamerauLevenshtein.getDistance("slime", "slime"), 0.01);
  }


}
