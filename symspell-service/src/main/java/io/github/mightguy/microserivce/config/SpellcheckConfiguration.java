package io.github.mightguy.microserivce.config;

import io.github.mightguy.spellcheck.symspell.api.CharDistance;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spellcheck")
public class SpellcheckConfiguration {

  private double deletionWeight;
  private double insertionWeight;
  private double replaceWeight;
  private double transpositionWeight;
  private String charDistance;
}
