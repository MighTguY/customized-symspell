
package io.github.mightguy.spellcheck.symspell.impl;

import io.github.mightguy.spellcheck.symspell.api.DataHolder;
import io.github.mightguy.spellcheck.symspell.api.HashFunction;
import io.github.mightguy.spellcheck.symspell.common.DictionaryItem;
import io.github.mightguy.spellcheck.symspell.common.SpellCheckSettings;
import io.github.mightguy.spellcheck.symspell.common.SpellHelper;
import io.github.mightguy.spellcheck.symspell.exception.SpellCheckException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class to create in memory dictionary for the items with term->frequency
 */
public class InMemoryDataHolder implements DataHolder {

  /**
   * Dictionary of unique correct spelling words, and the frequency count for each word
   */
  private Map<String, Double> wordsDictionary = new HashMap<>();
  private Map<String, Double> bigramsDictionary = new HashMap<>();
  private Map<String, String> exclusionDictionary = new HashMap<>();

  /**
   * Dictionary of unique words that are  below the count threshold for being considered correct
   * spellings.
   */
  private Map<String, Double> belowThresholdWords = new HashMap<>();

  /**
   * Dictionary that contains a mapping of lists of suggested correction words to the hashCodes of
   * the original words and the deletes derived from them. Collisions of hashCodes is tolerated,
   * because suggestions are ultimately verified via an edit distance function. A list of
   * suggestions might have a single suggestion, or multiple suggestions.
   */
  private Map<Long, String[]> deletes = new HashMap<>();


  /**
   * Spell check settings to use the values while ingesting the terms.
   */
  private SpellCheckSettings spellCheckSettings;

  private HashFunction hashFunction;

  public InMemoryDataHolder(
      SpellCheckSettings spellCheckSettings,
      HashFunction hashFunction) {
    this.spellCheckSettings = spellCheckSettings;
    this.hashFunction = hashFunction;
  }

  /**
   * Create/Update an entry in the dictionary. For every word there are deletes with an edit
   * distance of 1...maxEditDistance created and added to the dictionary. Every delete entry has a
   * suggestions list, which points to the original term(s) it was created from. The dictionary may
   * be dynamically updated (word frequency and new words) at any time by calling addItem
   *
   * @param dictionaryItem {@link DictionaryItem}
   * @return True if the word was added as a new correctly spelled word, or False if the word is
   * added as a below threshold word, or updates an existing correctly spelled word.
   */
  @Override
  public boolean addItem(final DictionaryItem dictionaryItem) throws SpellCheckException {

    if (dictionaryItem.getFrequency() <= 0 && spellCheckSettings.getCountThreshold() > 0) {
      return false;
    }

    double frequency = dictionaryItem.getFrequency();
    String key = dictionaryItem.getTerm();
    if (spellCheckSettings.isLowerCaseTerms()) {
      key = key.toLowerCase();
    }
    if (frequency <= 0) {
      frequency = 0;
    }

    /*
     * look first in below threshold words, update count, and allow
     * promotion to correct spelling word if count reaches threshold
     * threshold must be >1 for there to be the possibility of low
     * threshold words
     */

    frequency = addItemToBelowThreshold(key, frequency);

    if (frequency == Double.MIN_VALUE) {
      return false;
    }

    //Adding new threshold word
    if (!addToDictionary(key, frequency)) {
      return false;
    }


    /*
     * edits/suggestions are created only once, no matter how often
     * word occurs. edits/suggestions are created as soon as the
     * word occurs in the corpus, even if the same term existed
     * before in the dictionary as an edit from another word
     */
    if (key.length() > spellCheckSettings.getMaxLength()) {
      spellCheckSettings.setMaxLength(key.length());
    }

    //create deletes
    Set<String> editDeletes = SpellHelper
        .getEditDeletes(key, spellCheckSettings.getMaxEditDistance(),
            spellCheckSettings.getPrefixLength(), spellCheckSettings.getEditFactor());
    for (String delete : editDeletes) {
      Long hash = hashFunction.hash(delete);
      String[] suggestions;
      if (deletes.containsKey(hash)) {
        suggestions = deletes.get(hash);
        String[] newSuggestions = Arrays.copyOf(suggestions, suggestions.length + 1);
        deletes.put(hash, newSuggestions);
        suggestions = newSuggestions;
      } else {
        suggestions = new String[1];
        deletes.put(hash, suggestions);
      }
      suggestions[suggestions.length - 1] = key;
    }
    return true;
  }


  private boolean addToDictionary(String key, double frequency) {
    if (spellCheckSettings.isDoKeySplit()
        && key.split(spellCheckSettings.getKeySplitRegex()).length > 1) {
      bigramsDictionary.put(key, frequency);
      if (frequency < spellCheckSettings.getBigramCountMin()) {
        spellCheckSettings.setBigramCountMin(frequency);
      }
      return false;
    } else {
      wordsDictionary.put(key, frequency);
      return true;
    }
  }


  @Override
  public Double getItemFrequency(String term) throws SpellCheckException {
    return wordsDictionary.getOrDefault(term, null);
  }

  @Override
  public Double getItemFrequencyBiGram(String term) throws SpellCheckException {
    return bigramsDictionary.getOrDefault(term, null);
  }

  @Override
  public String[] getDeletes(String key) {
    return deletes.getOrDefault(hashFunction.hash(key), null);
  }

  @Override
  public int getSize() {
    return wordsDictionary.size();
  }

  @Override
  public boolean clear() {
    wordsDictionary.clear();
    deletes.clear();
    belowThresholdWords.clear();
    return false;
  }

  private double addItemToBelowThreshold(String key, double frequency) {
    if (spellCheckSettings.getCountThreshold() > 1 && belowThresholdWords.containsKey(key)) {
      double prevFreq = belowThresholdWords.get(key);
      frequency =
          prevFreq + (Double.MAX_VALUE - prevFreq > frequency ? frequency : Double.MAX_VALUE);
      if (frequency > spellCheckSettings.getCountThreshold()) {
        belowThresholdWords.remove(key);
      } else {
        belowThresholdWords.put(key, frequency);
        return Double.MIN_VALUE;
      }
    } else if (wordsDictionary.containsKey(key)) {
      double prevFreq = wordsDictionary.get(key);
      frequency =
          prevFreq + (Double.MAX_VALUE - prevFreq > frequency ? frequency : Double.MAX_VALUE);
      addToDictionary(key, frequency);
      return Double.MIN_VALUE;
    } else if (frequency < spellCheckSettings.getCountThreshold()) {
      belowThresholdWords.put(key, frequency);
      return Double.MIN_VALUE;
    }
    return frequency;
  }

  @Override
  public void addExclusionItem(String key, String value) {
    exclusionDictionary.put(key, value);
  }

  @Override
  public void addExclusionItems(Map<String, String> values) {
    exclusionDictionary.putAll(values);
  }

  @Override
  public String getExclusionItem(String key) {
    return exclusionDictionary.getOrDefault(key, null);
  }
}
