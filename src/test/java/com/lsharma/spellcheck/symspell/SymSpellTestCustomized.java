
package com.lsharma.spellcheck.symspell;

import com.lsharma.spellcheck.symspell.api.DataHolder;
import com.lsharma.spellcheck.symspell.common.DictionaryItem;
import com.lsharma.spellcheck.symspell.common.Murmur3HashFunction;
import com.lsharma.spellcheck.symspell.common.QwertyDistance;
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


public class SymSpellTestCustomized {

  static DataHolder dataHolder;
  static SymSpellCheck symSpellCheck;
  static WeightedDamerauLevenshteinDistance weightedDamerauLevenshteinDistance;

  @BeforeClass
  public static void setup() throws IOException, SpellCheckException {

    ClassLoader classLoader = SymSpellTest.class.getClassLoader();

    SpellCheckSettings spellCheckSettings = SpellCheckSettings.builder()
        .countThreshold(1)
        .deletionWeight(1f)
        .insertionWeight(1f)
        .replaceWeight(1f)
        .maxEditDistance(2)
        .transpositionWeight(1f)
        .topK(5)
        .prefixLength(10)
        .verbosity(Verbosity.ALL).build();

    weightedDamerauLevenshteinDistance =
        new WeightedDamerauLevenshteinDistance(spellCheckSettings.getDeletionWeight(),
            spellCheckSettings.getInsertionWeight(), spellCheckSettings.getReplaceWeight(),
            spellCheckSettings.getTranspositionWeight(), new QwertyDistance());
    dataHolder = new InMemoryDataHolder(spellCheckSettings, new Murmur3HashFunction());

    symSpellCheck = new SymSpellCheck(dataHolder, weightedDamerauLevenshteinDistance,
        spellCheckSettings);
    List<String> result = new ArrayList<>();
    loadUniGramFile(
        new File(classLoader.getResource("frequency_dictionary_en_82_765.txt").getFile()));
    loadBiGramFile(
        new File(classLoader.getResource("frequency_bigramdictionary_en_243_342.txt").getFile()));
  }

  private static void loadUniGramFile(File file) throws IOException, SpellCheckException {
    BufferedReader br = new BufferedReader(new FileReader(file));
    String line;
    while ((line = br.readLine()) != null) {
      String[] arr = line.split("\\s+");
      dataHolder.addItem(new DictionaryItem(arr[0], Double.parseDouble(arr[1]), -1.0));
    }
  }

  private static void loadBiGramFile(File file) throws IOException, SpellCheckException {
    BufferedReader br = new BufferedReader(new FileReader(file));
    String line;
    while ((line = br.readLine()) != null) {
      String[] arr = line.split("\\s+");
      dataHolder
          .addItem(new DictionaryItem(arr[0] + " " + arr[1], Double.parseDouble(arr[2]), -1.0));
    }
  }

  @Test
  public void testSingleWordCorrection() throws SpellCheckException {

    SymSpellTest.assertTypoAndCorrected(symSpellCheck,
        "uick", "quick", 2);
    SymSpellTest.assertTypoAndCorrected(symSpellCheck,
        "bigjest", "biggest", 2);
    SymSpellTest.assertTypoAndCorrected(symSpellCheck,
        "playrs", "players", 2);
    SymSpellTest.assertTypoAndCorrected(symSpellCheck,
        "slatew", "slates", 2);
    SymSpellTest.assertTypoAndCorrected(symSpellCheck,
        "ith", "with", 2);
    SymSpellTest.assertTypoAndCorrected(symSpellCheck,
        "plety", "plenty", 2);
    SymSpellTest.assertTypoAndCorrected(symSpellCheck,
        "funn", "fun", 2);
    SymSpellTest.assertTypoAndCorrected(symSpellCheck,
        "slives", "slices", 2);
  }

  @Test
  public void testDoubleWordCorrection() throws SpellCheckException {
    SymSpellTest.assertTypoAndCorrected(symSpellCheck, "Whereis", "where is", 2);
    SymSpellTest.assertTypoAndCorrected(symSpellCheck, "couqdn'tread", "couldn't read", 2);
    SymSpellTest.assertTypoAndCorrected(symSpellCheck, "hehaD", "he had", 2);

  }

  @Test
  public void testMultiWordCorrection() throws SpellCheckException {
    SymSpellTest.assertTypoAndCorrected(symSpellCheck,
        "Whereis th elove hehaD Dated FOREEVER forImuch of thepast who couqdn'tread in "
            + "sixthgrade AND ins pired him",
        "where is the love he had dated forever for much of the past who couldn't read "
            + "in sixth grade and inspired him",
        2);
  }

}
