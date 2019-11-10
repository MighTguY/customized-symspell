
package com.lsharma.spellcheck.symspell;

import com.lsharma.spellcheck.symspell.common.DictionaryItem;
import com.lsharma.spellcheck.symspell.common.Murmur3HashFunction;
import com.lsharma.spellcheck.symspell.common.SpellCheckSettings;
import com.lsharma.spellcheck.symspell.common.Verbosity;
import com.lsharma.spellcheck.symspell.exception.SpellCheckException;
import com.lsharma.spellcheck.symspell.impl.InMemoryDataHolder;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestInMemoryDataHolder {

  static InMemoryDataHolder dataHolder;

  @BeforeClass
  public static void setup() {

    SpellCheckSettings spellCheckSettings = SpellCheckSettings.builder()
        .countThreshold(4)
        .deletionWeight(0.8f)
        .insertionWeight(1.01f)
        .replaceWeight(0.9f)
        .maxEditDistance(2)
        .transpositionWeight(0.7f)
        .topK(5)
        .prefixLength(10)
        .verbosity(Verbosity.ALL).build();

    dataHolder = new InMemoryDataHolder(spellCheckSettings, new Murmur3HashFunction());
  }

  @Test
  public void testDataHolderAdd() throws SpellCheckException {
    dataHolder.addItem(new DictionaryItem("word", 12.0, -1.0));
    dataHolder.addItem(new DictionaryItem("word", 23.0, -1.0));
    dataHolder.addItem(new DictionaryItem("cold", 12.0, -1.0));
    dataHolder.addItem(new DictionaryItem("cool", 12.0, -1.0));
    dataHolder.addItem(new DictionaryItem("war", 12.0, -1.0));
    dataHolder.addItem(new DictionaryItem("dummy", 12.0, -1.0));
    dataHolder.addItem(new DictionaryItem("delta", 12.0, -1.0));

    Assert.assertEquals(6, dataHolder.getSize());
  }

  @Test
  public void testDataHolderGetItemFreq() throws SpellCheckException {
    dataHolder.addItem(new DictionaryItem("word", 21.0, -1.0));
    dataHolder.addItem(new DictionaryItem("cold", 22.0, -1.0));
    dataHolder.addItem(new DictionaryItem("cool", 23.0, -1.0));
    Assert.assertEquals(Double.valueOf(22.0), dataHolder.getItemFrequency("cold"));
    Assert.assertNull(dataHolder.getItemFrequency("hello"));
  }

  @Test
  public void testDataHolderGetDeletes() throws SpellCheckException {
    dataHolder.addItem(new DictionaryItem("word", 12.0, -1.0));
    dataHolder.addItem(new DictionaryItem("cold", 21.0, -1.0));
    dataHolder.addItem(new DictionaryItem("cool", 3.0, -1.0));
    dataHolder.addItem(new DictionaryItem("tool", 3.0, -1.0));
    dataHolder.addItem(new DictionaryItem("tool", 2.0, -1.0));

    Assert.assertNull(dataHolder.getItemFrequency("cool"));
    Assert.assertEquals(5.0, dataHolder.getItemFrequency("tool"), 0.01);
    Assert.assertEquals(12.0, dataHolder.getItemFrequency("word"), 0.01);
    Assert.assertEquals(21.0, dataHolder.getItemFrequency("cold"), 0.01);

    dataHolder.addItem(new DictionaryItem("cool", 8.0, -1.0));

    Assert.assertEquals(Double.valueOf(11.0), dataHolder.getItemFrequency("cool"));
    Assert.assertEquals(1, dataHolder.getDeletes("wod").length);
    Assert.assertEquals(2, dataHolder.getDeletes("col").length);
    Assert.assertFalse(dataHolder.addItem(new DictionaryItem("temp_data", 0.1, 0.0)));
    Assert.assertFalse(dataHolder.addItem(new DictionaryItem("temp_data", -0.1, 0.0)));
  }

  @After
  public void clear() throws SpellCheckException {
    dataHolder.clear();
  }
}
