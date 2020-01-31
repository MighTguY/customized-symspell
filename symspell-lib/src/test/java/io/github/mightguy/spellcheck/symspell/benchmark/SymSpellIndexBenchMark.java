package io.github.mightguy.spellcheck.symspell.benchmark;

import io.github.mightguy.spellcheck.symspell.api.CharDistance;
import io.github.mightguy.spellcheck.symspell.api.DataHolder;
import io.github.mightguy.spellcheck.symspell.api.SpellChecker;
import io.github.mightguy.spellcheck.symspell.api.StringDistance;
import io.github.mightguy.spellcheck.symspell.common.DictionaryItem;
import io.github.mightguy.spellcheck.symspell.common.Murmur3HashFunction;
import io.github.mightguy.spellcheck.symspell.common.SpellCheckSettings;
import io.github.mightguy.spellcheck.symspell.common.WeightedDamerauLevenshteinDistance;
import io.github.mightguy.spellcheck.symspell.exception.SpellCheckException;
import io.github.mightguy.spellcheck.symspell.impl.InMemoryDataHolder;
import io.github.mightguy.spellcheck.symspell.impl.SymSpellCheck;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
public class SymSpellIndexBenchMark {


  @Param({"1.0d", "2.0d", "3.0d"})
  public double maxEditDistance;

  @Param({"frequency_dictionary_en_30_000.txt", "frequency_dictionary_en_82_765.txt",
      "frequency_dictionary_en_500_000.txt"})
  public String dataFile;

  public String queryFile = "noisy_query_en_1000.txt";
  public SpellChecker spellChecker;
  private static long totalMatches = 0;

  @Setup(Level.Iteration)
  public void setup() throws SpellCheckException, IOException {
    SpellCheckSettings spellCheckSettings = SpellCheckSettings.builder()
        .maxEditDistance(maxEditDistance).build();

    DataHolder dataHolder = new InMemoryDataHolder(spellCheckSettings,
        new Murmur3HashFunction());

    spellChecker = new SymSpellCheck(dataHolder,
        getStringDistance(spellCheckSettings, null),
        spellCheckSettings);

  }


  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Measurement(iterations = 1, time = 10, timeUnit = TimeUnit.SECONDS)
  public void searchBenchmark() throws SpellCheckException, IOException, InterruptedException {
    indexData(dataFile, spellChecker.getDataHolder());
    System.out.println(" DataHolder Indexed Size " + spellChecker.getDataHolder().getSize());
    Thread.sleep(10000);
  }

  @TearDown(Level.Iteration)
  public void tearDown() {
    spellChecker = null;
  }

  private StringDistance getStringDistance(SpellCheckSettings spellCheckSettings,
      CharDistance charDistance) {
    return new WeightedDamerauLevenshteinDistance(spellCheckSettings.getDeletionWeight(),
        spellCheckSettings.getInsertionWeight(),
        spellCheckSettings.getReplaceWeight(),
        spellCheckSettings.getTranspositionWeight(), charDistance);
  }

  private void indexData(String dataResourceName, DataHolder dataHolder)
      throws IOException, SpellCheckException {
    URL resourceUrl = this.getClass().getClassLoader().getResource(dataResourceName);
    CSVParser parser = CSVParser
        .parse(resourceUrl, Charset.forName("UTF-8"), CSVFormat.DEFAULT.withDelimiter(' '));
    java.util.Iterator<CSVRecord> csvIterator = parser.iterator();
    while (csvIterator.hasNext()) {
      CSVRecord csvRecord = csvIterator.next();
      dataHolder
          .addItem(new DictionaryItem(csvRecord.get(0), Double.valueOf(csvRecord.get(1)), 0d));
    }
  }


  @Test
  public void testBenchmarkIndex() throws RunnerException, IOException {
    File file = checkFileAndCreate(SymSpellIndexBenchMark.class.getName());
    Options opt = new OptionsBuilder()
        .include(SymSpellIndexBenchMark.class.getSimpleName())
        .addProfiler(MemoryProfiler.class.getName())
        .resultFormat(ResultFormatType.JSON)
        .result(file.getAbsolutePath())
        .warmupIterations(0)
        .measurementIterations(1)
        .forks(1)
        .build();
    new Runner(opt).run();
    System.out.println("Total Lookup results instance " + totalMatches);

  }

  private File checkFileAndCreate(String name) throws IOException {
    String targetFolderPath = Paths.get(
        this.getClass().getResource("/").getFile()).getParent().toString() + "/benchmark-result/";

    File targetFolder = new File(targetFolderPath);
    targetFolder.mkdirs();

    File file = new File(
        targetFolder + SymSpellIndexBenchMark.class.getSimpleName()
            + "_" + System.currentTimeMillis() + ".json");
    if (file.exists()) {
      file.delete();
    }
    file.createNewFile();
    return file;
  }
}
