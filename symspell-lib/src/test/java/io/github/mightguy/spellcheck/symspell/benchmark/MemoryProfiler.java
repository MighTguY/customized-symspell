package io.github.mightguy.spellcheck.symspell.benchmark;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Collection;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.profile.InternalProfiler;
import org.openjdk.jmh.results.AggregationPolicy;
import org.openjdk.jmh.results.Defaults;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.ScalarResult;

public class MemoryProfiler implements InternalProfiler {

  @Override
  public String getDescription() {
    return "memory heap profiler";
  }

  @Override
  public void beforeIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams) {

  }

  @Override
  public Collection<? extends Result> afterIteration(BenchmarkParams bp, IterationParams ip,
      IterationResult result) {
    MemoryUsage heapUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
    MemoryUsage nonheapUsage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();

    Collection<ScalarResult> results = new ArrayList<>();
    results.add(
        new ScalarResult(Defaults.PREFIX + "mem.heap", heapUsage.getUsed() / (1024 * 1024.0), "MB",
            AggregationPolicy.MAX));
    results.add(new ScalarResult(
        Defaults.PREFIX + "mem.nonheap", nonheapUsage.getUsed() / (1024 * 1024.0), "MB",
        AggregationPolicy.MAX));

    return results;
  }

}