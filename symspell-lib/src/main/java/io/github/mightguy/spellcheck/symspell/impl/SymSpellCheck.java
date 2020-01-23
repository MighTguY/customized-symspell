
package io.github.mightguy.spellcheck.symspell.impl;

import io.github.mightguy.spellcheck.symspell.api.DataHolder;
import io.github.mightguy.spellcheck.symspell.api.SpellChecker;
import io.github.mightguy.spellcheck.symspell.api.StringDistance;
import io.github.mightguy.spellcheck.symspell.common.Composition;
import io.github.mightguy.spellcheck.symspell.common.SpellCheckSettings;
import io.github.mightguy.spellcheck.symspell.common.SpellHelper;
import io.github.mightguy.spellcheck.symspell.common.SuggestionItem;
import io.github.mightguy.spellcheck.symspell.common.Verbosity;
import io.github.mightguy.spellcheck.symspell.exception.SpellCheckException;
import io.github.mightguy.spellcheck.symspell.exception.SpellCheckExceptionCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Symspell variant of the Spellchecker
 */
public class SymSpellCheck extends SpellChecker {

  //N equals the sum of all counts c in the dictionary only if the dictionary is complete,
  // but not if the dictionary is truncated or filtered
  private static Long nMax = 1024908267229L;

  public SymSpellCheck(DataHolder dataHolder,
      StringDistance stringDistance,
      SpellCheckSettings spellCheckSettings) {
    super(dataHolder, stringDistance, spellCheckSettings);
  }


  /**
   * supports compound aware automatic spelling correction of multi-word input strings with three
   * cases 1. mistakenly inserted space into a correct word led to two incorrect terms 2. mistakenly
   * omitted space between two correct words led to one incorrect combined term 3. multiple
   * independent input terms with/without spelling errors Find suggested spellings for a multi-word
   * input string (supports word splitting/merging).
   *
   * @param phrase The string being spell checked.
   * @param maxEditDistance The maximum edit distance between input and suggested words.
   * @return A list of {@link SuggestionItem} object representing suggested correct spellings for
   * the input string.
   */
  @Override
  public List<SuggestionItem> lookupCompound(String phrase, double maxEditDistance)
      throws SpellCheckException {

    if (maxEditDistance > spellCheckSettings.getMaxEditDistance()) {
      throw new SpellCheckException(SpellCheckExceptionCode.LOOKUP_ERROR,
          "max Edit distance should be less than  global Max i.e" + spellCheckSettings
              .getMaxEditDistance());
    }
    if (StringUtils.isEmpty(phrase)) {
      throw new SpellCheckException(SpellCheckExceptionCode.LOOKUP_ERROR,
          "Invalid input of string");
    }
    if (spellCheckSettings.isLowerCaseTerms()) {
      phrase = phrase.toLowerCase();
    }
    String[] items = SpellHelper.tokenizeOnWhiteSpace(phrase);
    List<SuggestionItem> suggestions = new ArrayList<>();
    List<SuggestionItem> suggestionParts = new ArrayList<>();
    boolean isLastCombi = false;

    /*
      Early exit when in exclusion list
     */
    if (StringUtils.isNotEmpty(dataHolder.getExclusionItem(phrase))) {
      return SpellHelper
          .earlyExit(suggestions, dataHolder.getExclusionItem(phrase), maxEditDistance, false);
    }

    for (int i = 0; i < items.length; i++) {
      //Normal suggestions
      suggestions = lookup(items[i], Verbosity.TOP, maxEditDistance);

      //combi check, always before split
      if (i > 0 && !isLastCombi
          && lookupCombineWords(items[i], items[i - 1], suggestions, suggestionParts,
          maxEditDistance)) {
        isLastCombi = true;
        continue;
      }

      isLastCombi = false;

      if (CollectionUtils.isNotEmpty(suggestions) && (suggestions.get(0).getDistance() == 0
          || items[i].length() == 1)) {
        //choose best suggestion
        suggestionParts.add(suggestions.get(0));
      } else {
        lookupSplitWords(suggestionParts, suggestions, items[i], maxEditDistance);
      }


    }

    String joinedTerm = "";
    double joinedCount = Double.MAX_VALUE;
    for (SuggestionItem si : suggestionParts) {
      joinedTerm = joinedTerm.concat(si.getTerm()).concat(" ");
      joinedCount = Math.min(joinedCount, si.getCount());
    }
    double dist = stringDistance.getDistance(
        joinedTerm.trim(), phrase, Math.pow(2, 31) - 1);

    SuggestionItem suggestionItem = new SuggestionItem(joinedTerm, dist, joinedCount);
    return Collections.singletonList(suggestionItem);
  }

  /**
   * supports compound aware automatic spelling correction of multi-word input strings with
   * mistakenly omitted space between two correct words led to one incorrect combined term
   *
   * @param token The string being spell checked.
   * @param previousToken The string previousToken being spell checked.
   * @param maxEditDistance The maximum edit distance between input and suggested words.
   * @param suggestions Suggestions items List
   * @param suggestionParts Partial suggestions list.
   */
  private boolean lookupCombineWords(String token, String previousToken,
      List<SuggestionItem> suggestions,
      List<SuggestionItem> suggestionParts, double maxEditDistance) throws SpellCheckException {
    List<SuggestionItem> suggestionsCombi = lookup(previousToken + token, Verbosity.TOP,
        maxEditDistance);
    if (CollectionUtils.isEmpty(suggestionsCombi)) {
      return false;
    }
    SuggestionItem best1 = suggestionParts.get(suggestionParts.size() - 1);
    SuggestionItem best2;
    if (CollectionUtils.isNotEmpty(suggestions)) {
      best2 = suggestions.get(0);
    } else {
      best2 = new SuggestionItem(token,
          maxEditDistance + 1, 0);
    }

    double editDistance = stringDistance
        .getDistance(best1.getTerm().concat(" ").concat(best2.getTerm()),
            previousToken.concat(" ").concat(token), maxEditDistance);

    if (editDistance >= 0 && suggestionsCombi.get(0).getDistance() < editDistance) {
      suggestionsCombi.get(0).setDistance(suggestionsCombi.get(0).getDistance() + 1);
      suggestionParts.remove(suggestionParts.size() - 1);
      suggestionParts.add(suggestionsCombi.get(0));
      return true;
    }
    return false;
  }


  /**
   * supports compound aware automatic spelling correction of multi-word input strings with
   * mistakenly inserted space into a correct word led to two incorrect terms
   *
   * @param suggestions Suggestions items List
   * @param maxEditDistance The maximum edit distance between input and suggested words.
   * @param suggestionParts Partial suggestions list.
   */
  private void lookupSplitWords(List<SuggestionItem> suggestionParts,
      List<SuggestionItem> suggestions, String word, double maxEditDistance)
      throws SpellCheckException {

    //if no perfect suggestion, split word into pairs
    SuggestionItem suggestionSplitBest = null;
    if (CollectionUtils.isNotEmpty(suggestions)) {
      suggestionSplitBest = suggestions.get(0);
    }

    if (word.length() <= 1) {
      suggestionParts.add(new SuggestionItem(word, maxEditDistance + 1, 0));
      return;
    }

    for (int j = 1; j < word.length(); j++) {
      String part1 = word.substring(0, j);
      String part2 = word.substring(j, word.length());

      List<SuggestionItem> suggestions1 = lookup(part1, Verbosity.TOP,
          maxEditDistance);

      if (SpellHelper.continueConditionIfHeadIsSame(suggestions, suggestions1)) {
        continue;
      }

      List<SuggestionItem> suggestions2 = lookup(part2, Verbosity.TOP, maxEditDistance);

      if (SpellHelper.continueConditionIfHeadIsSame(suggestions, suggestions2)) {
        continue;
      }

      String split = suggestions1.get(0).getTerm() + " " + suggestions2.get(0).getTerm();
      double splitDistance = stringDistance.getDistance(word, split, maxEditDistance);
      double count;

      if (splitDistance < 0) {
        splitDistance = maxEditDistance + 1;
      }

      if (suggestionSplitBest != null) {
        if (splitDistance > suggestionSplitBest.getDistance()) {
          continue;
        }
        if (splitDistance < suggestionSplitBest.getDistance()) {
          suggestionSplitBest = null;
        }
      }

      Double bigramFreq = dataHolder.getItemFrequencyBiGram(split);

      //if bigram exists in bigram dictionary
      if (bigramFreq != null) {
        count = bigramFreq;

        if (CollectionUtils.isNotEmpty(suggestions)) {
          if ((suggestions1.get(0).getTerm() + suggestions2.get(0).getTerm()).equals(word)) {
            //make count bigger than count of single term correction
            count = Math.max(count, suggestions.get(0).getCount() + 2);
          } else if ((suggestions1.get(0).getTerm() == suggestions.get(0).getTerm())
              || (suggestions2.get(0).getTerm().equals(suggestions.get(0).getTerm()))) {
            //make count bigger than count of single term correction
            count = Math.max(count, suggestions.get(0).getCount() + 1);
          }
        } else if ((suggestions1.get(0).getTerm() + suggestions2.get(0).getTerm()).equals(word)) {
          count = Math.max(count,
              Math.max(suggestions1.get(0).getCount(), suggestions2.get(0).getCount()));
        }
      } else {
        count = Math.min(spellCheckSettings.getBigramCountMin(),
            (suggestions1.get(0).getCount() / nMax
                * suggestions2.get(0).getCount()));
      }

      SuggestionItem suggestionSplit = new SuggestionItem(split, splitDistance, count);

      if ((suggestionSplitBest == null) || (suggestionSplit.getCount() > suggestionSplitBest
          .getCount())) {
        suggestionSplitBest = suggestionSplit;
      }

    }

    if (suggestionSplitBest != null) {
      suggestionParts.add(suggestionSplitBest);
    } else {
      suggestionParts.add(new SuggestionItem(word, maxEditDistance + 1, 0));
    }

  }

  /**
   * @param phrase The word being spell checked.
   * @param verbosity The value controlling the quantity/closeness of the returned suggestions
   * @param maxEditDistance The maximum edit distance between phrase and suggested words.
   * @return List of {@link SuggestionItem}
   */
  @Override
  public List<SuggestionItem> lookup(String phrase, Verbosity verbosity,
      double maxEditDistance)
      throws SpellCheckException {

    if (maxEditDistance <= 0) {
      maxEditDistance = spellCheckSettings.getMaxEditDistance();
    }

    if (maxEditDistance > spellCheckSettings.getMaxEditDistance()) {
      throw new SpellCheckException(SpellCheckExceptionCode.LOOKUP_ERROR,
          "max Edit distance should be less than  global Max i.e" + spellCheckSettings
              .getMaxEditDistance());
    }

    int phraseLen = phrase.length();
    if (spellCheckSettings.isLowerCaseTerms()) {
      phrase = phrase.toLowerCase();
    }
    double suggestionCount = 0;
    Set<String> consideredDeletes = new HashSet<>();
    Set<String> consideredSuggestions = new HashSet<>();
    List<SuggestionItem> suggestionItems = new ArrayList<>(
        spellCheckSettings.getTopK());

    /*
      Early exit when in exclusion list
     */
    if (StringUtils.isNotEmpty(dataHolder.getExclusionItem(phrase))) {
      return SpellHelper
          .earlyExit(suggestionItems, dataHolder.getExclusionItem(phrase), maxEditDistance, false);
    }

    /*
    Early exit when word is too big
     */
    if ((phraseLen - maxEditDistance) > spellCheckSettings.getMaxLength()) {
      return SpellHelper.earlyExit(suggestionItems, phrase, maxEditDistance,
          spellCheckSettings.isIgnoreUnknown());
    }

    Double frequency = dataHolder.getItemFrequency(phrase);

    if (frequency != null) {
      suggestionCount = frequency;
      suggestionItems.add(new SuggestionItem(phrase, 0, suggestionCount));

      if (verbosity != Verbosity.ALL) {
        return SpellHelper.earlyExit(suggestionItems, phrase, maxEditDistance,
            spellCheckSettings.isIgnoreUnknown());
      }
    }

    consideredSuggestions.add(phrase);
    double maxEditDistance2 = maxEditDistance;
    final int phrasePrefixLen;
    List<String> candidates = new ArrayList<>();

    if (phraseLen > spellCheckSettings.getPrefixLength()) {
      phrasePrefixLen = spellCheckSettings.getPrefixLength();
      candidates.add(phrase.substring(0, phrasePrefixLen));
    } else {
      phrasePrefixLen = phraseLen;
    }
    candidates.add(phrase);

    while (CollectionUtils.isNotEmpty(candidates)) {
      String candidate = candidates.remove(0);
      int candidateLen = candidate.length();
      int lenDiff = phraseLen - candidateLen;
      /*
      early termination: if candidate distance is already higher than suggestion distance,
      than there are no better suggestions to be expected
       */
      if (lenDiff > maxEditDistance2) {
        if (verbosity == Verbosity.ALL) {
          continue;
        }
        break;
      }


      /*
      read candidate entry from dictionary
       */
      String[] deletes = dataHolder.getDeletes(candidate);
      if (deletes != null && deletes.length > 0) {

        for (String suggestion : deletes) {
          if (
              filterOnEquivalance(suggestion, phrase, candidate, maxEditDistance2)
                  ||
                  filterOnPrefixLen(suggestion.length(), spellCheckSettings.getPrefixLength(),
                      phrasePrefixLen, candidate.length(), maxEditDistance2)) {
            continue;
          }
          /*
            True Damerau-Levenshtein Edit Distance: adjust
                    distance, if both distances>0
                    We allow simultaneous edits (deletes) of
                    max_edit_distance on on both the dictionary and
                    the phrase term. For replaces and adjacent
                    transposes the resulting edit distance stays
                    <= max_edit_distance. For inserts and deletes the
                    resulting edit distance might exceed
                    max_edit_distance. To prevent suggestions of a
                    higher edit distance, we need to calculate the
                    resulting edit distance, if there are
                    simultaneous edits on both sides.
                    Example: (bank==bnak and bank==bink, but
                    bank!=kanb and bank!=xban and bank!=baxn for
                    max_edit_distance=1). Two deletes on each side of
                    a pair makes them all equal, but the first two
                    pairs have edit distance=1, the others edit
                    distance=2.
          */
          double distance = 0;
          int minDistance = 0;

          if (candidateLen == 0) {
            /*
              suggestions which have no common chars with
                        phrase (phrase_len<=max_edit_distance &&
                        suggestion_len<=max_edit_distance)
            */
            distance = Math.max(phraseLen, suggestion.length());
            if (distance > maxEditDistance2 || !consideredSuggestions.add(suggestion)) {
              continue;
            }
          } else if (suggestion.length() == 1) {
            distance = phrase.indexOf(suggestion.charAt(0)) < 0 ? phraseLen : phraseLen - 1;
            if (distance > maxEditDistance2 || !consideredSuggestions.add(suggestion)) {
              continue;
            }
          } else {

            /*
              handles the shortcircuit of min_distance assignment when first boolean expression
              evaluates to False
             */

            minDistance = getMinDistanceOnPrefixbasis(maxEditDistance, candidate,
                phrase, suggestion);

            if (isDistanceCalculationRequired(phrase, maxEditDistance, minDistance, suggestion,
                candidate)) {
              continue;
            } else {
              if (verbosity != Verbosity.ALL
                  && !deleteInSuggestionPrefix(candidate, candidateLen,
                  suggestion, suggestion.length()) || !consideredSuggestions
                  .add(suggestion)) {
                continue;
              }
              distance = stringDistance.getDistance(phrase, suggestion, maxEditDistance2);
              if (distance < 0) {
                continue;
              }
            }
          }

          if (SpellHelper.isLessOrEqualDouble(distance, maxEditDistance2, 0.01)) {
            suggestionCount = dataHolder.getItemFrequency(suggestion);
            SuggestionItem si = new SuggestionItem(suggestion, distance, suggestionCount);
            if (CollectionUtils.isNotEmpty(suggestionItems)) {
              if (verbosity == Verbosity.CLOSEST && distance < maxEditDistance2) {
                suggestionItems.clear();
              } else if (verbosity == Verbosity.TOP) {
                if (SpellHelper.isLessDouble(distance, maxEditDistance2, 0.01)
                    || suggestionCount > suggestionItems.get(0).getCount()) {
                  maxEditDistance2 = distance;
                  suggestionItems.set(0, si);
                }
                continue;
              }
            }

            if (verbosity != Verbosity.ALL) {
              maxEditDistance2 = distance;
            }
            suggestionItems.add(si);
          }
        }
      }

      if (lenDiff < maxEditDistance && candidateLen <= spellCheckSettings.getPrefixLength()) {
        if (verbosity != Verbosity.ALL && lenDiff >= maxEditDistance2) {
          continue;
        }

        for (int i = 0; i < candidateLen; i++) {
          String delete = candidate.substring(0, i) + candidate.substring(i + 1, candidateLen);
          if (consideredDeletes.add(delete)) {
            candidates.add(delete);
          }
        }

      }
    }

    return suggestionItems;
  }


  private int getMinDistanceOnPrefixbasis(double maxEditDistance, String candidate, String
      phrase,
      String suggestion) {
    if ((spellCheckSettings.getPrefixLength() - maxEditDistance) == candidate.length()) {
      return
          Math.min(phrase.length(), suggestion.length()) - spellCheckSettings.getPrefixLength();
    } else {
      return 0;
    }
  }

  private boolean filterOnPrefixLen(int suggestionLen, int prefixLen, int phrasePrefixLen,
      int candidateLen,
      double maxEditDistance2) {
    int suggestionPrefixLen = Math.min(suggestionLen, prefixLen);
    return (suggestionPrefixLen > phrasePrefixLen
        && (suggestionPrefixLen - candidateLen) > maxEditDistance2);
  }

  private boolean filterOnEquivalance(String delete, String phrase, String candidate,
      double maxEditDistance2) {

    return (delete.equals(phrase)
        || (Math.abs(delete.length() - phrase.length()) > maxEditDistance2)
        || (delete.length() < candidate.length()) || (delete.length() == candidate.length()
        && !delete.equals(candidate)));
  }

  /**
   * Check whether all delete chars are present in the suggestion prefix in correct order, otherwise
   * this is just a hash collision
   */
  private boolean deleteInSuggestionPrefix(String delete, int deleteLen, String suggestion,
      int suggestionLen) {
    if (deleteLen == 0) {
      return true;
    }
    if (spellCheckSettings.getPrefixLength() < suggestionLen) {
      suggestionLen = spellCheckSettings.getPrefixLength();
    }

    int j = 0;
    for (int i = 0; i < deleteLen; i++) {
      char delChar = delete.charAt(i);
      while (j < suggestionLen && delChar != suggestion.charAt(j)) {
        j++;
      }
      if (j == suggestionLen) {
        return false;
      }
    }
    return true;
  }

  private boolean isDistanceCalculationRequired(String phrase, double maxEditDistance, int min,
      String suggestion, String candidate) {
    return phrase.length() - maxEditDistance == candidate.length()
        && (min > 1
        && !(phrase.substring(phrase.length() + 1 - min)
        .equals(suggestion.substring(suggestion.length() + 1 - min))))
        || (min > 0
        && phrase.charAt(phrase.length() - min) != suggestion
        .charAt(suggestion.length() - min)
        && phrase.charAt(phrase.length() - min - 1) != suggestion
        .charAt(suggestion.length() - min)
        && phrase.charAt(phrase.length() - min) != suggestion
        .charAt(suggestion.length() - min - 1));
  }

  /**
   * word_segmentation` divides a string into words by inserting missing spaces at the appropriate
   * positions misspelled words are corrected and do not affect segmentation existing spaces are
   * allowed and considered for optimum segmentation
   *
   * `word_segmentation` uses a novel approach *without* recursion. https://medium.com/@wolfgarbe/fast-word-segmentation-for-noisy-text-2c2c41f9e8da
   * While each string of length n can be segmented in 2^n−1 possible compositions
   * https://en.wikipedia.org/wiki/Composition_(combinatorics) `word_segmentation` has a linear
   * runtime O(n) to find the optimum composition
   *
   * Find suggested spellings for a multi-word input string (supports word splitting/merging).
   *
   * @param phrase The string being spell checked.
   * @param maxSegmentationWordLength The maximum word length
   * @param maxEditDistance The maximum edit distance
   * @return The word segmented string
   */
  public Composition wordBreakSegmentation(String phrase, int maxSegmentationWordLength,
      double maxEditDistance) throws SpellCheckException {

    /*
    number of all words in the corpus used to generate the
        frequency dictionary. This is used to calculate the word
        occurrence probability p from word counts c : p=c/nMax. nMax equals
        the sum of all counts c in the dictionary only if the
        dictionary is complete, but not if the dictionary is
        truncated or filtered
     */
    if (phrase.isEmpty()) {
      return new Composition();
    }
    if (spellCheckSettings.isLowerCaseTerms()) {
      phrase = phrase.toLowerCase();
    }

    /*
      Early exit when in exclusion list
     */
    if (StringUtils.isNotEmpty(dataHolder.getExclusionItem(phrase))) {
      return new Composition(phrase, dataHolder.getExclusionItem(phrase), 0, 0);
    }

    int arraySize = Math.min(maxSegmentationWordLength, phrase.length());
    Composition[] compositions = new Composition[arraySize];
    for (int i = 0; i < arraySize; i++) {
      compositions[i] = new Composition();
    }
    int circularIndex = -1;
    //outer loop (column): all possible part start positions
    for (int j = 0; j < phrase.length(); j++) {
      //inner loop (row): all possible part lengths (from start position): part can't be bigger
      // than longest word in dictionary (other than long unknown word)
      int imax = Math.min(phrase.length() - j, maxSegmentationWordLength);
      for (int i = 1; i <= imax; i++) {
        //get top spelling correction/ed for part
        String part = phrase.substring(j, j + i);
        int separatorLength = 0;
        int topEd = 0;
        double topProbabilityLog;
        String topResult;
        if (Character.isWhitespace(part.charAt(0))) {
          //remove space for levensthein calculation
          part = part.substring(1);
        } else {
          //add ed+1: space did not exist, had to be inserted
          separatorLength = 1;
        }

        //remove space from part1, add number of removed spaces to topEd
        topEd += part.length();
        //remove space
        part = part
            .replace(" ", "");
        //add number of removed spaces to ed
        topEd -= part.length();

        List<SuggestionItem> results = this.lookup(part, Verbosity.TOP, maxEditDistance);
        if (CollectionUtils.isNotEmpty(results)) {
          topResult = results.get(0).getTerm();
          topEd += results.get(0).getDistance();
          //Naive Bayes Rule
          //we assume the word probabilities of two words to be independent
          //therefore the resulting probability of the word combination is the product of the
          // two word probabilities

          //instead of computing the product of probabilities we are computing the sum of the
          // logarithm of probabilities
          //because the probabilities of words are about 10^-10, the product of many such small
          // numbers could exceed (underflow) the floating number range and become zero
          //log(ab)=log(a)+log(b)
          topProbabilityLog = Math.log10(results.get(0).getCount() / nMax);
        } else {
          topResult = part;
          //default, if word not found
          //otherwise long input text would win as long unknown word (with ed=edmax+1 ), although
          // there there should many spaces inserted
          topEd += part.length();
          topProbabilityLog = Math.log10(10.0 / (nMax * Math.pow(10.0, part.length())));
        }
        int destinationIndex = ((i + circularIndex) % arraySize);

        //set values in first loop
        if (j == 0) {
          compositions[destinationIndex].setSegmentedString(part);
          compositions[destinationIndex].setCorrectedString(topResult);
          compositions[destinationIndex].setDistanceSum(topEd);
          compositions[destinationIndex].setLogProbSum(topProbabilityLog);
        } else if ((i == maxSegmentationWordLength)
            //replace values if better probabilityLogSum, if same edit distance OR one
            // space difference
            || (((compositions[circularIndex].getDistanceSum() + topEd
            == compositions[destinationIndex].getDistanceSum()) || (
            compositions[circularIndex].getDistanceSum() + separatorLength + topEd
                == compositions[destinationIndex].getDistanceSum())) && (
            compositions[destinationIndex].getLogProbSum()
                < compositions[circularIndex].getLogProbSum() + topProbabilityLog))
            //replace values if smaller edit distance
            || (compositions[circularIndex].getDistanceSum() + separatorLength + topEd
            < compositions[destinationIndex].getDistanceSum())) {
          compositions[destinationIndex].setSegmentedString(
              compositions[circularIndex].getSegmentedString() + " " + part);
          compositions[destinationIndex].setCorrectedString(
              compositions[circularIndex].getCorrectedString() + " " + topResult);
          compositions[destinationIndex].setDistanceSum(
              compositions[circularIndex].getDistanceSum() + topEd);
          compositions[destinationIndex].setLogProbSum(
              compositions[circularIndex].getLogProbSum() + topProbabilityLog);
        }
      }
      circularIndex++;
      if (circularIndex >= arraySize) {
        circularIndex = 0;
      }
    }
    return compositions[circularIndex];
  }


}
