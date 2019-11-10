package com.lsharma.spellcheck.symspell;

import com.lsharma.spellcheck.symspell.api.DataHolder;
import com.lsharma.spellcheck.symspell.common.DictionaryItem;
import com.lsharma.spellcheck.symspell.common.Murmur3HashFunction;
import com.lsharma.spellcheck.symspell.common.SpellCheckSettings;
import com.lsharma.spellcheck.symspell.common.SuggestionItem;
import com.lsharma.spellcheck.symspell.common.WeightedDamerauLevenshteinDistance;
import com.lsharma.spellcheck.symspell.exception.SpellCheckException;
import com.lsharma.spellcheck.symspell.impl.InMemoryDataHolder;
import com.lsharma.spellcheck.symspell.impl.SymSpellCheck;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
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
    suggestions.stream()
        .limit(10)
        .forEach(suggestion -> log.info(
            "Lookup suggestion: "
                + suggestion.getTerm() + " "
                + suggestion.getDistance() + " "
                + suggestion.getCount()));
    log.info("LookupCompound: " + compound.getTerm());
  }

  private void suggestItemOnConsole() throws IOException, SpellCheckException {
    String inputTerm;
    Scanner sc = new Scanner(System.in);
    while (true) {
      log.info("Please enter the term to get the suggest Item");
      inputTerm = sc.next();
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
