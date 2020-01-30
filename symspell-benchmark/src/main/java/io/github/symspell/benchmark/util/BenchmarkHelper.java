package io.github.symspell.benchmark.util;

import io.github.mightguy.spellcheck.symspell.api.DataHolder;
import io.github.mightguy.spellcheck.symspell.api.SpellChecker;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

@Getter
public class BenchmarkHelper {

  private final Map<String, Double> words = new HashMap<>();
  private final List<Query> queries = new ArrayList<>();

  public BenchmarkHelper(String dataResourceName, String queryResourceName) throws IOException {
    URL resourceUrl = this.getClass().getClassLoader().getResource(dataResourceName);
    CSVParser parser = CSVParser
        .parse(resourceUrl, Charset.forName("UTF-8"), CSVFormat.DEFAULT.withDelimiter(' '));
    Iterator<CSVRecord> csvIterator = parser.iterator();
    while (csvIterator.hasNext()) {
      CSVRecord csvRecord = csvIterator.next();
      words.put(csvRecord.get(0), Double.valueOf(csvRecord.get(1)));
    }

    URL queryResourceUrl = this.getClass().getClassLoader().getResource(queryResourceName);
    CSVParser qparser = CSVParser
        .parse(queryResourceUrl, Charset.forName("UTF-8"), CSVFormat.DEFAULT.withDelimiter(' '));
    csvIterator = qparser.iterator();
    while (csvIterator.hasNext()) {
      CSVRecord csvRecord = csvIterator.next();
      queries.add(new Query(csvRecord.get(0), csvRecord.get(1), Double.valueOf(csvRecord.get(2))));
    }
    System.out.println(queries.size() + " Query Size");
    System.out.println(words.size() + " Data Size");
  }

  public boolean index(SpellChecker spellChecker) throws SpellCheckException {
    int count = 0;
    for (Map.Entry<String, Double> entry : words.entrySet()) {
      spellChecker.getDataHolder().addItem(new DictionaryItem(entry.getKey(), entry.getValue(),
          spellChecker.getSpellCheckSettings().getMaxEditDistance()));
      count++;
    }
    System.out.println("Ingestion Completed " + count);
    return true;
  }

  public boolean testResult(Query searchQuery, SpellChecker spellChecker, Verbosity verbosity)
      throws SpellCheckException {
    List<SuggestionItem> suggestionItems = spellChecker
        .lookup(searchQuery.getTestString(), verbosity,
            spellChecker.getSpellCheckSettings().getMaxEditDistance());
    return suggestionItems.parallelStream()
        .anyMatch(s -> s.getTerm().equals(searchQuery.getExpectedString()));
  }

  public SpellChecker getDefaultSymSpellChecker() {
    SpellCheckSettings spellCheckSettings = SpellCheckSettings.builder().maxEditDistance(5).build();

    WeightedDamerauLevenshteinDistance weightedDamerauLevenshteinDistance =
        new WeightedDamerauLevenshteinDistance(spellCheckSettings.getDeletionWeight(),
            spellCheckSettings.getInsertionWeight(), spellCheckSettings.getReplaceWeight(),
            spellCheckSettings.getTranspositionWeight(), null);
    DataHolder dataHolder = new InMemoryDataHolder(spellCheckSettings, new Murmur3HashFunction());

    return new SymSpellCheck(dataHolder, weightedDamerauLevenshteinDistance,
        spellCheckSettings);
  }

}
