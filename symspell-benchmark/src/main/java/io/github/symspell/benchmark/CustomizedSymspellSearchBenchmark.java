package io.github.symspell.benchmark;

import io.github.mightguy.spellcheck.symspell.api.SpellChecker;
import io.github.mightguy.spellcheck.symspell.common.Verbosity;
import io.github.mightguy.spellcheck.symspell.exception.SpellCheckException;
import io.github.symspell.benchmark.util.BenchmarkHelper;
import io.github.symspell.benchmark.util.Query;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;


public class CustomizedSymspellSearchBenchmark {

  @State(Scope.Benchmark)
  public static class Data {

    public SpellChecker spellChecker;
    public List<Query> queries;
    public BenchmarkHelper benchmarkHelper;


    @Setup
    public void setUp() throws IOException, SpellCheckException {
      benchmarkHelper = new BenchmarkHelper("frequency_dictionary_en_500_000.txt",
          "noisy_query_en_1000.txt");
      spellChecker = benchmarkHelper.getDefaultSymSpellChecker();
      queries = benchmarkHelper.getQueries();
      benchmarkHelper.index(spellChecker);
    }
  }

  @State(Scope.Thread)
  public static class Iterator {

    private int n = 0;

    Query getNextQuery(List<Query> queries) {
      if (n >= queries.size()) {
        n = 0;
      }
      return queries.get(n++);
    }
  }


  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  public void testSearchBenchMark(Data data, Iterator i, Blackhole blackhole)
      throws SpellCheckException {
    Query query = null;
    while (query == null) {
      query = i.getNextQuery(data.queries);
    }
    boolean res = data.benchmarkHelper.testResult(query, data.spellChecker, Verbosity.TOP);
    blackhole.consume(res);
  }



  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(CustomizedSymspellSearchBenchmark.class.getSimpleName())
        .warmupIterations(0)
        .measurementIterations(1)
        .forks(1)
        .build();
    new Runner(opt).run();
  }
}
