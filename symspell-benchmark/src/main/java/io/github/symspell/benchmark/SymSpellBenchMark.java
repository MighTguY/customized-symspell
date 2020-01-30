package io.github.symspell.benchmark;

import io.github.mightguy.spellcheck.symspell.api.DataHolder;
import io.github.mightguy.spellcheck.symspell.api.SpellChecker;
import io.github.mightguy.spellcheck.symspell.api.StringDistance;
import io.github.mightguy.spellcheck.symspell.common.DictionaryItem;
import io.github.mightguy.spellcheck.symspell.common.Murmur3HashFunction;
import io.github.mightguy.spellcheck.symspell.common.SpellCheckSettings;
import io.github.mightguy.spellcheck.symspell.common.SuggestionItem;
import io.github.mightguy.spellcheck.symspell.common.Verbosity;
import io.github.mightguy.spellcheck.symspell.common.WeightedDamerauLevenshteinDistance;
import io.github.mightguy.spellcheck.symspell.exception.SpellCheckException;
import io.github.mightguy.spellcheck.symspell.impl.InMemoryDataHolder;
import io.github.mightguy.spellcheck.symspell.impl.SymSpellCheck;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Assert;

public class SymSpellBenchMark {

  String[] dataFiles = {
//      "frequency_dictionary_en_30_000.txt",
//      "frequency_dictionary_en_82_765.txt",
      "frequency_dictionary_en_500_000.txt"
  };

  String[] queryFiles = {
      "noisy_query_en_1000.txt"
  };

  SpellCheckSettings basicSpellCheckerSettings = getDefaultSymSpellCheckerSettings();
  SpellCheckSettings custmoizedSpellCheckerSettings = getDefaultSymSpellCheckerSettings();
  StringDistance basicSpellCheckerDistance = getDefaultSymSpellCheckerDistance();

  private StringDistance getDefaultSymSpellCheckerDistance() {
    return new WeightedDamerauLevenshteinDistance(basicSpellCheckerSettings.getDeletionWeight(),
        basicSpellCheckerSettings.getInsertionWeight(),
        basicSpellCheckerSettings.getReplaceWeight(),
        basicSpellCheckerSettings.getTranspositionWeight(), null);
  }

  public SpellCheckSettings getDefaultSymSpellCheckerSettings() {
    SpellCheckSettings spellCheckSettings = SpellCheckSettings.builder().maxEditDistance(5).build();

    return spellCheckSettings;

  }


  void warmUp() throws IOException, SpellCheckException {
    DataHolder dataHolder = new InMemoryDataHolder(basicSpellCheckerSettings,
        new Murmur3HashFunction());
    indexData(dataFiles[0], dataHolder);
    SpellChecker basicSpellChecker = new SymSpellCheck(dataHolder,
        basicSpellCheckerDistance,
        basicSpellCheckerSettings);

    List<SuggestionItem> suggestionItemList = basicSpellChecker
        .lookup("hockie", Verbosity.ALL, 1);
    Collections.sort(suggestionItemList);
    List<SuggestionItem> compundSuggestions = basicSpellChecker
        .lookupCompound("hockie", 1);
    Assert.assertNotNull(suggestionItemList);
    Assert.assertNotNull(compundSuggestions);
  }

  private void indexData(String dataResourceName, DataHolder dataHolder)
      throws IOException, SpellCheckException {
    URL resourceUrl = this.getClass().getClassLoader().getResource(dataResourceName);
    CSVParser parser = CSVParser
        .parse(resourceUrl, Charset.forName("UTF-8"), CSVFormat.DEFAULT.withDelimiter(' '));
    Iterator<CSVRecord> csvIterator = parser.iterator();
    while (csvIterator.hasNext()) {
      CSVRecord csvRecord = csvIterator.next();
      dataHolder
          .addItem(new DictionaryItem(csvRecord.get(0), Double.valueOf(csvRecord.get(1)), 0d));
    }
  }

  public static void main(String[] args) throws IOException, SpellCheckException {
    SymSpellBenchMark symSpellBenchMark = new SymSpellBenchMark();
//    symSpellBenchMark.warmUp();
    symSpellBenchMark.benchmarkPrecalculationLookup();
  }

  public void benchmarkPrecalculationLookup() throws IOException, SpellCheckException {
    int resultNumber = 0;
    int repetitions = 1000;
    int totalLoopCount = 0;
    long totalMatches = 0;
    long totalOrigMatches = 0;
    double totalLoadTime, totalMem, totalLookupTime, totalOrigLoadTime, totalOrigMem, totalOrigLookupTime;
    totalLoadTime = totalMem = totalLookupTime = totalOrigLoadTime = totalOrigMem = totalOrigLookupTime = 0;
    long totalRepetitions = 0;
    List<String> query1K = buildQuery1K();
    StopWatch stopWatch = StopWatch.createStarted();
    for (int maxEditDistance = 1; maxEditDistance <= 3; maxEditDistance++) {
      //benchmark dictionary precalculation size and time
      //maxEditDistance=1/2/3; prefixLength=5/6/7;  dictionary=30k/82k/500k; class=instantiated/static
      for (int i = 0; i < dataFiles.length; i++) {
        totalLoopCount++;
        long memSize = Runtime.getRuntime().totalMemory();
        stopWatch.reset();
        stopWatch.start();
        //SymspellInstance:
        SpellCheckSettings spellCheckSettings = SpellCheckSettings.builder()
            .maxEditDistance(maxEditDistance)
            .build();
        long prefixLength = spellCheckSettings.getPrefixLength();
        DataHolder dataHolder = new InMemoryDataHolder(basicSpellCheckerSettings,
            new Murmur3HashFunction());

        //Indexing data
        indexData(dataFiles[i], dataHolder);
        stopWatch.stop();
        long memDelta = Runtime.getRuntime().totalMemory() - memSize;
        totalLoadTime += stopWatch.getTime(TimeUnit.MILLISECONDS);
        totalMem += memDelta / 1024.0 / 1024.0;
        System.out.println(
            "Precalculation instance " + stopWatch.getTime(TimeUnit.MILLISECONDS) + "ms " + (
                memDelta / 1024.0 / 1024.0)
                + "MB " + dataHolder.getSize() + " words " + " MaxEditDistance=" + maxEditDistance
                + " prefixLength=" + prefixLength + " dict=" + dataFiles[i]);

        SpellChecker basicSpellChecker = new SymSpellCheck(dataHolder,
            basicSpellCheckerDistance,
            basicSpellCheckerSettings);

//        for (Verbosity verbosity : Verbosity.values()) {
        Verbosity verbosity = Verbosity.TOP;
//          //instantiated exact
//          stopWatch.reset();
//          stopWatch.start();
//          for (int round = 0; round < repetitions; round++) {
//            resultNumber = basicSpellChecker.lookup("different", verbosity, maxEditDistance)
//                .size();
//          }
//          stopWatch.stop();
//          totalLookupTime += stopWatch.getTime(TimeUnit.NANOSECONDS);
//          totalMatches += resultNumber;
//          System.out.println("Lookup instance " + resultNumber + " results " + (
//              stopWatch.getTime(TimeUnit.NANOSECONDS) / repetitions)
//              + "ns/op verbosity=" + verbosity + " query=exact");
//          totalRepetitions += repetitions;
//
//          //instantiated non-exact
//          stopWatch.reset();
//          stopWatch.start();
//          for (int round = 0; round < repetitions; round++) {
//            resultNumber = basicSpellChecker.lookup("hockie", verbosity, maxEditDistance).size();
//          }
//          stopWatch.stop();
//          totalLookupTime += stopWatch.getTime(TimeUnit.NANOSECONDS);
//          totalMatches += resultNumber;
//          System.out.println("Lookup instance " + resultNumber + " results " + (
//              stopWatch.getTime(TimeUnit.NANOSECONDS) / repetitions)
//              + "ns/op verbosity=" + verbosity + " query=non-exact");
//          totalRepetitions += repetitions;
//
//          //instantiated mix
        stopWatch.reset();
        stopWatch.start();
        resultNumber = 0;
        for (String word : query1K) {
          resultNumber += basicSpellChecker.lookup(word, verbosity, maxEditDistance).size();
        }
        stopWatch.stop();
        totalLookupTime += stopWatch.getTime(TimeUnit.NANOSECONDS);
        totalMatches += resultNumber;
        System.out.println("Lookup instance " + resultNumber + " results " + (
            stopWatch.getTime(TimeUnit.NANOSECONDS) / query1K.size())
            + "ns/op verbosity=" + verbosity + " query=mix");
        totalRepetitions += repetitions;
//        }
        System.out.println();
        dataHolder.clear();
        dataHolder = null;
        basicSpellChecker = null;
        System.out.println("Cleaning GC started");
        System.gc();
        System.out.println("Cleaning GC completed");
      }

    }

    System.out.println(
        "Average Precalculation time instance " + (totalLoadTime / totalLoopCount) + "ms");

    System.out.println(
        "Average Precalculation memory instance " + (totalMem / totalLoopCount) + "MB ");

    System.out.println(
        "Average Lookup time instance " + (totalLookupTime / totalRepetitions) + "ns");

    System.out.println("Total Lookup results instance " + totalMatches);


  }

  private List<String> buildQuery1K() throws IOException {
    List<String> testList = new ArrayList<>();

    URL resourceUrl = this.getClass().getClassLoader().getResource(queryFiles[0]);
    CSVParser parser = CSVParser
        .parse(resourceUrl, Charset.forName("UTF-8"), CSVFormat.DEFAULT.withDelimiter(' '));
    Iterator<CSVRecord> csvIterator = parser.iterator();
    while (csvIterator.hasNext()) {
      CSVRecord csvRecord = csvIterator.next();
      testList.add(csvRecord.get(0));
    }
    return testList;
  }
}
