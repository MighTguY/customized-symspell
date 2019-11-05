
package com.lsharma.spellcheck.symspell.api;

import com.lsharma.spellcheck.symspell.common.DictionaryItem;
import com.lsharma.spellcheck.symspell.exception.SpellCheckException;

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

}
