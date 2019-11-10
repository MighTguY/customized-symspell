package com.lsharma.spellcheck.symspell;

import com.lsharma.spellcheck.symspell.api.DataHolder;
import com.lsharma.spellcheck.symspell.api.SpellChecker;
import com.lsharma.spellcheck.symspell.api.StringDistance;
import com.lsharma.spellcheck.symspell.common.DictionaryItem;
import com.lsharma.spellcheck.symspell.common.Murmur3HashFunction;
import com.lsharma.spellcheck.symspell.common.SpellCheckSettings;
import com.lsharma.spellcheck.symspell.common.SpellHelper;
import com.lsharma.spellcheck.symspell.common.SuggestionItem;
import com.lsharma.spellcheck.symspell.common.Verbosity;
import com.lsharma.spellcheck.symspell.common.WeightedDamerauLevenshteinDistance;
import com.lsharma.spellcheck.symspell.impl.InMemoryDataHolder;
import com.lsharma.spellcheck.symspell.impl.SymSpellCheck;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class MiscUnitTest {

  @Test
  public void testDictionaryItem() {
    DictionaryItem di = new DictionaryItem("term", 1.0, 2.0);
    Assert.assertEquals("term", di.getTerm());
    Assert.assertEquals(Double.valueOf(1.0), di.getFrequency());
    Assert.assertEquals(Double.valueOf(2.0), di.getDistance());
  }

  @Test
  public void testSpellCheckSettings() {
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

    spellCheckSettings.setMaxLength(1);
    Assert.assertNotNull(spellCheckSettings);
    Assert.assertEquals(1, spellCheckSettings.getMaxLength());
    Assert.assertTrue(spellCheckSettings.toString().contains("countThreshold"));
    Assert.assertTrue(spellCheckSettings.toString().length() > 20);
  }

  @Test
  public void testSpellChecker() {
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

    StringDistance weightedDamerauLevenshteinDistance =
        new WeightedDamerauLevenshteinDistance(spellCheckSettings.getDeletionWeight(),
            spellCheckSettings.getInsertionWeight(), spellCheckSettings.getReplaceWeight(),
            spellCheckSettings.getTranspositionWeight(), null);
    DataHolder dataHolder = new InMemoryDataHolder(spellCheckSettings, new Murmur3HashFunction());

    SpellChecker symSpellCheck = new SymSpellCheck(dataHolder, weightedDamerauLevenshteinDistance,
        spellCheckSettings);

    Assert.assertEquals(dataHolder, symSpellCheck.getDataHolder());
    Assert.assertEquals(weightedDamerauLevenshteinDistance, symSpellCheck.getStringDistance());
    Assert.assertEquals(spellCheckSettings, symSpellCheck.getSpellCheckSettings());
  }

  @Test
  public void testSpellDeletes() {
    Set<String> del = SpellHelper.getEditDeletes("a", 2.0, 0);
    Assert.assertNotNull(del);
    Assert.assertEquals(1, del.size());

    Set<String> del1 = SpellHelper.edits("", 2.0, del, 2.0);
    Assert.assertNotNull(del);
    Assert.assertEquals(del1, del);
  }

  @Test
  public void testEarlyExit() {
    List<SuggestionItem> suggestionItems = new ArrayList<>();
    List<SuggestionItem> suggestionItems1 = SpellHelper.earlyExit(suggestionItems, "term", 2.0);

    Assert.assertNotNull(suggestionItems1);
    Assert.assertEquals(0, suggestionItems1.size());
  }

  @Test
  public void suggestItemTest() {
    SuggestionItem si = new SuggestionItem("term1", 1.0, 20);
    SuggestionItem si2 = new SuggestionItem("term2", 1.001, 20);
    Assert.assertEquals(0, si.compare(si, si2));

    si = new SuggestionItem("term1", 1.0, 20);
    si2 = new SuggestionItem("term2", 2.001, 20);
    Assert.assertEquals(-1, si.compare(si, si2));

    si = new SuggestionItem("term1", 2.0, 20);
    si2 = new SuggestionItem("term2", 1.001, 20);
    Assert.assertEquals(1, si.compare(si, si2));

    si = new SuggestionItem("term1", 1.0, 21);
    si2 = new SuggestionItem("term2", 1.001, 20);
    Assert.assertEquals(-1, si.compare(si, si2));

    si = new SuggestionItem("term1", 1.0, 20);
    si2 = new SuggestionItem("term2", 1.001, 21);
    Assert.assertEquals(1, si.compare(si, si2));

    si = new SuggestionItem("term1", 1.0, 20);
    si2 = new SuggestionItem("term2", 1.001, 21);
    Assert.assertFalse(si.equals(si2));

    si = new SuggestionItem("term1", 1.0, 20);
    si2 = new SuggestionItem("term1", 1.001, 20);
    Assert.assertFalse(si.equals(si2));

    Assert.assertTrue(si.toString().length() > 10);
  }

}
