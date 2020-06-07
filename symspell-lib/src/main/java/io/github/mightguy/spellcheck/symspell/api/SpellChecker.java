
package io.github.mightguy.spellcheck.symspell.api;

import io.github.mightguy.spellcheck.symspell.common.Composition;
import io.github.mightguy.spellcheck.symspell.common.SpellCheckSettings;
import io.github.mightguy.spellcheck.symspell.common.SuggestionItem;
import io.github.mightguy.spellcheck.symspell.common.Verbosity;
import io.github.mightguy.spellcheck.symspell.exception.SpellCheckException;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Abstract class for the Spell Correction
 */
@AllArgsConstructor
@Getter
public abstract class SpellChecker {

  protected DataHolder dataHolder;

  protected StringDistance stringDistance;

  protected SpellCheckSettings spellCheckSettings;


  public List<SuggestionItem> lookup(String word) throws SpellCheckException {
    return lookup(word, spellCheckSettings.getVerbosity(), spellCheckSettings.getMaxEditDistance());
  }

  public List<SuggestionItem> lookup(String word, Verbosity verbosity) throws SpellCheckException {
    return lookup(word, verbosity, spellCheckSettings.getMaxEditDistance());
  }

  public abstract List<SuggestionItem> lookup(String word, Verbosity verbosity, double editDistance)
      throws SpellCheckException;


  public List<SuggestionItem> lookupCompound(String word) throws SpellCheckException {
    return lookupCompound(word, spellCheckSettings.getMaxEditDistance());
  }


  public abstract List<SuggestionItem> lookupCompound(String word, double editDistance,
      boolean tokenizeOnWhiteSpace)
      throws SpellCheckException;

  public List<SuggestionItem> lookupCompound(String word, double editDistance)
      throws SpellCheckException {
    return lookupCompound(word, editDistance, true);
  }


  public Composition wordBreakSegmentation(String phrase) throws SpellCheckException {
    return wordBreakSegmentation(phrase, spellCheckSettings.getPrefixLength(),
        spellCheckSettings.getMaxEditDistance());
  }

  public Composition wordBreakSegmentation(String phrase, Double ed) throws SpellCheckException {
    return wordBreakSegmentation(phrase, spellCheckSettings.getPrefixLength(),
        ed);
  }

  public abstract Composition wordBreakSegmentation(String phrase, int maxSegmentationWordLength,
      double maxEditDistance) throws SpellCheckException;

}
