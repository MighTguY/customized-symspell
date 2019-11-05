
package com.lsharma.spellcheck.symspell;

import com.lsharma.spellcheck.symspell.api.DataHolder;
import com.lsharma.spellcheck.symspell.common.DictionaryItem;
import com.lsharma.spellcheck.symspell.common.Murmur3HashFunction;
import com.lsharma.spellcheck.symspell.common.SpellCheckSettings;
import com.lsharma.spellcheck.symspell.common.Verbosity;
import com.lsharma.spellcheck.symspell.common.WeightedDamerauLevenshteinDistance;
import com.lsharma.spellcheck.symspell.exception.SpellCheckException;
import com.lsharma.spellcheck.symspell.impl.InMemoryDataHolder;
import com.lsharma.spellcheck.symspell.impl.SymSpellCheck;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;


public class SymSpellTestWeighted {

  static DataHolder dataHolder;
  static SymSpellCheck symSpellCheck;
  static WeightedDamerauLevenshteinDistance weightedDamerauLevenshteinDistance;

  @BeforeClass
  public static void setup() throws IOException, SpellCheckException {

    ClassLoader classLoader = SymSpellTest.class.getClassLoader();

    SpellCheckSettings spellCheckSettings = SpellCheckSettings.builder()
        .countThreshold(1)
        .deletionWeight(0.8f)
        .insertionWeight(1.01f)
        .replaceWeight(1.5f)
        .maxEditDistance(2)
        .transpositionWeight(0.7f)
        .topK(5)
        .prefixLength(10)
        .verbosity(Verbosity.ALL).build();

    weightedDamerauLevenshteinDistance =
        new WeightedDamerauLevenshteinDistance(spellCheckSettings.getDeletionWeight(),
            spellCheckSettings.getInsertionWeight(), spellCheckSettings.getReplaceWeight(),
            spellCheckSettings.getTranspositionWeight(), null);
    dataHolder = new InMemoryDataHolder(spellCheckSettings, new Murmur3HashFunction());

    symSpellCheck = new SymSpellCheck(dataHolder, weightedDamerauLevenshteinDistance,
        spellCheckSettings);
    List<String> result = new ArrayList<>();
    File file = new File(classLoader.getResource("frequency_dictionary_en_82_765.txt").getFile());
    BufferedReader br = new BufferedReader(new FileReader(file));
    String line;
    while ((line = br.readLine()) != null) {
      String[] arr = line.split("\\s+");
      dataHolder.addItem(new DictionaryItem(arr[0], Double.parseDouble(arr[1]), -1.0));
    }
  }

  @Test
  public void testSingleWordCorrection() throws SpellCheckException {

    SymSpellTest.assertTypoAndCorrected(symSpellCheck,
        "uick", "quick", 2);
    SymSpellTest.assertTypoAndCorrected(symSpellCheck,
        "bigjest", "big jest", 2);
    SymSpellTest.assertTypoAndCorrected(symSpellCheck,
        "playrs", "plays", 2);
    SymSpellTest.assertTypoAndCorrected(symSpellCheck,
        "slatew", "slate", 2);
    SymSpellTest.assertTypoAndCorrected(symSpellCheck,
        "ith", "with", 2);
    SymSpellTest.assertTypoAndCorrected(symSpellCheck,
        "plety", "plenty", 2);
    SymSpellTest.assertTypoAndCorrected(symSpellCheck,
        "funn", "fun", 2);


  }

  @Test
  public void testDoubleWordCorrection() throws SpellCheckException {
    SymSpellTest.assertTypoAndCorrected(symSpellCheck,
        "theq uick brown f ox jumps over the lazy dog",
        "the quick brown fox jumps over the lazy dog",
        2);

  }

  @Test
  public void testMultiWordCorrection() throws SpellCheckException {
    SymSpellTest.assertTypoAndCorrected(symSpellCheck,
        "theq uick brown f ox jumps over the lazy dog",
        "the quick brown fox jumps over the lazy dog",
        2);
  }

}
