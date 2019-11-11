
package io.github.mightguy.spellcheck.symspell.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Composition {

  private String segmentedString;
  private String correctedString;
  private int distanceSum;
  private double logProbSum;

}
