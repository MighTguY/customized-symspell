package io.github.symspell.benchmark.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Query {

  private String testString;
  private String expectedString;
  private double editDistance;
}
