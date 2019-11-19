package io.github.mightguy.spellcheckconsole;

import io.github.mightguy.spellcheck.symspell.api.DataHolder;
import io.github.mightguy.spellcheck.symspell.common.Composition;
import io.github.mightguy.spellcheck.symspell.common.DictionaryItem;
import io.github.mightguy.spellcheck.symspell.common.Murmur3HashFunction;
import io.github.mightguy.spellcheck.symspell.common.SpellCheckSettings;
import io.github.mightguy.spellcheck.symspell.common.SuggestionItem;
import io.github.mightguy.spellcheck.symspell.common.WeightedDamerauLevenshteinDistance;
import io.github.mightguy.spellcheck.symspell.exception.SpellCheckException;
import io.github.mightguy.spellcheck.symspell.impl.InMemoryDataHolder;
import io.github.mightguy.spellcheck.symspell.impl.SymSpellCheck;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpellCheckerConsole {

  private DataHolder dataHolder;
  private SymSpellCheck symSpellCheck;


  private boolean init() throws IOException, SpellCheckException {
    ClassLoader classLoader = SpellCheckerConsole.class.getClassLoader();

    SpellCheckSettings spellCheckSettings = SpellCheckSettings.builder().build();

    WeightedDamerauLevenshteinDistance weightedDamerauLevenshteinDistance =
        new WeightedDamerauLevenshteinDistance(spellCheckSettings.getDeletionWeight(),
            spellCheckSettings.getInsertionWeight(), spellCheckSettings.getReplaceWeight(),
            spellCheckSettings.getTranspositionWeight(), null);
    dataHolder = new InMemoryDataHolder(spellCheckSettings, new Murmur3HashFunction());

    symSpellCheck = new SymSpellCheck(dataHolder, weightedDamerauLevenshteinDistance,
        spellCheckSettings);
    loadUniGramFile(
        new File(classLoader.getResource("frequency_dictionary_en_82_765.txt").getFile()));
    loadBiGramFile(
        new File(classLoader.getResource("frequency_bigramdictionary_en_243_342.txt").getFile()));

    return false;
  }

  private void loadUniGramFile(File file) throws IOException, SpellCheckException {
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] arr = line.split("\\s+");
        dataHolder.addItem(new DictionaryItem(arr[0], Double.parseDouble(arr[1]), -1.0));
      }
    }
  }

  private void loadBiGramFile(File file) throws IOException, SpellCheckException {
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] arr = line.split("\\s+");
        dataHolder
            .addItem(new DictionaryItem(arr[0] + " " + arr[1], Double.parseDouble(arr[2]), -1.0));
      }
    }
  }

  private void suggestItem(String[] args) throws IOException, SpellCheckException {
    if (args.length > 0) {
      suggestItemOnArgs(args[0]);
    } else {
      suggestItemOnConsole();
    }

  }

  private void suggestItemOnArgs(String inputTerm) throws IOException, SpellCheckException {
    List<SuggestionItem> suggestions = symSpellCheck.lookup(inputTerm);
    SuggestionItem compound = symSpellCheck.lookupCompound(inputTerm).get(0);
    Composition composition = symSpellCheck.wordBreakSegmentation(inputTerm, 10, 2);

    suggestions.stream()
        .limit(10)
        .forEach(suggestion -> System.out.println(
            "Lookup suggestion: "
                + suggestion.getTerm() + " "
                + suggestion.getDistance() + " "
                + suggestion.getCount()));
    System.out.println("LookupCompound: " + compound.getTerm());
    System.out.println("Composition: " + composition.getCorrectedString());
  }

  private void suggestItemOnConsole() throws IOException, SpellCheckException {
    String inputTerm;
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      System.out.println("Please enter the term to get the suggest Item");
      inputTerm = reader.readLine();
      if (inputTerm.equalsIgnoreCase("q")) {
        return;
      }
      suggestItemOnArgs(inputTerm);
    }
  }

  public static void main(String[] args) throws IOException, SpellCheckException {
    SpellCheckerConsole spellCheckerConsole = new SpellCheckerConsole();
    spellCheckerConsole.init();
    spellCheckerConsole.suggestItem(args);
  }
}
