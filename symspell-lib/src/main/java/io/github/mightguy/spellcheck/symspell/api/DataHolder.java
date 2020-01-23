
package io.github.mightguy.spellcheck.symspell.api;

import io.github.mightguy.spellcheck.symspell.common.DictionaryItem;
import io.github.mightguy.spellcheck.symspell.exception.SpellCheckException;
import java.util.Map;

/**
 * Interface to contain the dictionary
 */
public interface DataHolder {

  public boolean addItem(DictionaryItem dictionaryItem) throws SpellCheckException;

  public Double getItemFrequency(String term) throws SpellCheckException;

  public Double getItemFrequencyBiGram(String term) throws SpellCheckException;

  public String[] getDeletes(String key) throws SpellCheckException;

  public int getSize();

  public boolean clear() throws SpellCheckException;

  public void addExclusionItem(String key, String value);

  public void addExclusionItems(Map<String, String> values);

  public String getExclusionItem(String key);
}
