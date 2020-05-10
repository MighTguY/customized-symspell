package io.github.mightguy.spellcheck.symspell;

import io.github.mightguy.spellcheck.symspell.api.DataHolder;
import io.github.mightguy.spellcheck.symspell.common.DictionaryItem;
import io.github.mightguy.spellcheck.symspell.common.Murmur3HashFunction;
import io.github.mightguy.spellcheck.symspell.common.QwertzDistance;
import io.github.mightguy.spellcheck.symspell.common.SpellCheckSettings;
import io.github.mightguy.spellcheck.symspell.common.SuggestionItem;
import io.github.mightguy.spellcheck.symspell.common.Verbosity;
import io.github.mightguy.spellcheck.symspell.common.WeightedDamerauLevenshteinDistance;
import io.github.mightguy.spellcheck.symspell.exception.SpellCheckException;
import io.github.mightguy.spellcheck.symspell.impl.InMemoryDataHolder;
import io.github.mightguy.spellcheck.symspell.impl.SymSpellCheck;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class GermanLangSpellChecker {

  static DataHolder dataHolder1;
  static DataHolder dataHolder2;
  static SymSpellCheck symSpellCheck;
  static SymSpellCheck qwertzSymSpellCheck;
  static WeightedDamerauLevenshteinDistance weightedDamerauLevenshteinDistance;
  static WeightedDamerauLevenshteinDistance qwertzWeightedDamerauLevenshteinDistance;

  @BeforeClass
  public static void setup() throws IOException, SpellCheckException {

    ClassLoader classLoader = SymSpellTest.class.getClassLoader();

    SpellCheckSettings spellCheckSettings = SpellCheckSettings.builder()
        .countThreshold(1)
        .deletionWeight(1)
        .insertionWeight(1)
        .replaceWeight(1)
        .maxEditDistance(2)
        .transpositionWeight(1)
        .topK(5)
        .prefixLength(10)
        .verbosity(Verbosity.ALL).build();

    weightedDamerauLevenshteinDistance =
        new WeightedDamerauLevenshteinDistance(spellCheckSettings.getDeletionWeight(),
            spellCheckSettings.getInsertionWeight(), spellCheckSettings.getReplaceWeight(),
            spellCheckSettings.getTranspositionWeight(), null);

    qwertzWeightedDamerauLevenshteinDistance =
        new WeightedDamerauLevenshteinDistance(spellCheckSettings.getDeletionWeight(),
            spellCheckSettings.getInsertionWeight(), spellCheckSettings.getReplaceWeight(),
            spellCheckSettings.getTranspositionWeight(), new QwertzDistance());

    dataHolder1 = new InMemoryDataHolder(spellCheckSettings, new Murmur3HashFunction());
    dataHolder2 = new InMemoryDataHolder(spellCheckSettings, new Murmur3HashFunction());

    symSpellCheck = new SymSpellCheck(dataHolder1, weightedDamerauLevenshteinDistance,
        spellCheckSettings);

    qwertzSymSpellCheck = new SymSpellCheck(dataHolder2, qwertzWeightedDamerauLevenshteinDistance,
        spellCheckSettings);

    List<String> result = new ArrayList<>();
    loadUniGramFile(
        new File(classLoader.getResource("de-100k.txt").getFile()));

  }

  private static void loadUniGramFile(File file) throws IOException, SpellCheckException {
    BufferedReader br = new BufferedReader(new FileReader(file));
    String line;
    while ((line = br.readLine()) != null) {
      String[] arr = line.split("\\s+");
      dataHolder1.addItem(new DictionaryItem(arr[0], Double.parseDouble(arr[1]), -1.0));
      dataHolder2.addItem(new DictionaryItem(arr[0], Double.parseDouble(arr[1]), -1.0));
    }
  }

  @Test
  public void testMultiWordCorrection() throws SpellCheckException {

    assertTypoAndCorrected(symSpellCheck,
        "entwick lung".toLowerCase(),
        "entwicklung".toLowerCase(),
        2);

    assertTypoEdAndCorrected(symSpellCheck,
        "nömlich".toLowerCase(),
        "nämlich".toLowerCase(),
        2, 1);

    assertTypoEdAndCorrected(qwertzSymSpellCheck,
        "nömlich".toLowerCase(),
        "nämlich".toLowerCase(),
        2, 0.10);

  }

  public static void assertTypoAndCorrected(SymSpellCheck spellCheck, String typo, String correct,
      double maxEd) throws SpellCheckException {
    List<SuggestionItem> suggestionItems = spellCheck
        .lookupCompound(typo.toLowerCase().trim(), maxEd);
    Assert.assertTrue(suggestionItems.size() > 0);
    Assert.assertEquals(correct.toLowerCase().trim(), suggestionItems.get(0).getTerm().trim());
  }

  public static void assertTypoEdAndCorrected(SymSpellCheck spellCheck, String typo, String correct,
      double maxEd, double expED) throws SpellCheckException {
    List<SuggestionItem> suggestionItems = spellCheck
        .lookupCompound(typo.toLowerCase().trim(), maxEd);
    Assert.assertTrue(suggestionItems.size() > 0);
    Assert.assertEquals(correct.toLowerCase().trim(), suggestionItems.get(0).getTerm().trim());
    Assert.assertEquals(suggestionItems.get(0).getDistance(), expED, 0.12);
  }
}
