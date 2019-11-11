
package io.github.mightguy.spellcheck.symspell.common;

import io.github.mightguy.spellcheck.symspell.api.CharDistance;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

/**
 * Qwerty distance to get the distance between two chars, the default distance is 1.5f It uses 0.1
 * for direct connect and 0.4f for the diagonal connect. Adjacency matrix of the Qwerty is computed
 * using
 * <a href="http://www.nada.kth.se/~ann/exjobb/axel_samuelsson.pdf">Qwerty Adjacency Map</a>
 */
@Slf4j
public class QwertyDistance implements CharDistance {

  double directConnect = 0.1f;
  double diagonalConnect = 0.4f;
  double defaultValue = 1f;

  double[][] operationCost = new double['z' + 1]['z' + 1];

  public QwertyDistance() {
    this.initializeCostMatrix();
  }

  @Override
  public double distance(char a, char b) {

    if (a > 'z' + 1 || b > 'z' + 1) {
      return defaultValue;
    }
    return this.operationCost[a][b];
  }

  public void initializeCostMatrix() {
    for (double[] row : this.operationCost) {
      Arrays.fill(row, defaultValue);
    }

    for (int i = 'a'; i <= 'z'; i++) {
      for (int j = 'a'; j <= 'z'; j++) {
        if (i == j) {
          this.operationCost[i][j] = 0;
        }
      }
    }

    this.operationCost['a']['s'] = directConnect;
    this.operationCost['a']['w'] = diagonalConnect;
    this.operationCost['a']['q'] = diagonalConnect;
    this.operationCost['a']['z'] = diagonalConnect;

    this.operationCost['s']['a'] = directConnect;
    this.operationCost['s']['d'] = directConnect;
    this.operationCost['s']['w'] = diagonalConnect;
    this.operationCost['s']['e'] = diagonalConnect;
    this.operationCost['s']['x'] = diagonalConnect;
    this.operationCost['s']['z'] = diagonalConnect;

    this.operationCost['d']['s'] = directConnect;
    this.operationCost['d']['f'] = directConnect;
    this.operationCost['d']['e'] = diagonalConnect;
    this.operationCost['d']['r'] = diagonalConnect;
    this.operationCost['d']['c'] = diagonalConnect;
    this.operationCost['d']['x'] = diagonalConnect;

    this.operationCost['f']['d'] = directConnect;
    this.operationCost['f']['g'] = directConnect;
    this.operationCost['f']['r'] = diagonalConnect;
    this.operationCost['f']['t'] = diagonalConnect;
    this.operationCost['f']['c'] = diagonalConnect;
    this.operationCost['f']['v'] = diagonalConnect;

    this.operationCost['g']['f'] = directConnect;
    this.operationCost['g']['h'] = directConnect;
    this.operationCost['g']['t'] = diagonalConnect;
    this.operationCost['g']['y'] = diagonalConnect;
    this.operationCost['g']['v'] = diagonalConnect;
    this.operationCost['g']['b'] = diagonalConnect;

    this.operationCost['h']['g'] = directConnect;
    this.operationCost['h']['j'] = directConnect;
    this.operationCost['h']['y'] = diagonalConnect;
    this.operationCost['h']['u'] = diagonalConnect;
    this.operationCost['h']['b'] = diagonalConnect;
    this.operationCost['h']['n'] = diagonalConnect;

    this.operationCost['j']['h'] = directConnect;
    this.operationCost['j']['k'] = directConnect;
    this.operationCost['j']['u'] = diagonalConnect;
    this.operationCost['j']['i'] = diagonalConnect;
    this.operationCost['j']['n'] = diagonalConnect;
    this.operationCost['j']['m'] = diagonalConnect;

    this.operationCost['k']['j'] = directConnect;
    this.operationCost['k']['l'] = directConnect;
    this.operationCost['k']['i'] = diagonalConnect;
    this.operationCost['k']['o'] = diagonalConnect;
    this.operationCost['k']['m'] = diagonalConnect;

    this.operationCost['l']['k'] = directConnect;
    this.operationCost['l']['o'] = diagonalConnect;
    this.operationCost['l']['p'] = diagonalConnect;

    this.operationCost['q']['w'] = directConnect;
    this.operationCost['q']['a'] = diagonalConnect;

    this.operationCost['w']['q'] = directConnect;
    this.operationCost['w']['e'] = directConnect;
    this.operationCost['w']['a'] = diagonalConnect;
    this.operationCost['w']['s'] = diagonalConnect;

    this.operationCost['e']['w'] = directConnect;
    this.operationCost['e']['r'] = directConnect;
    this.operationCost['e']['s'] = diagonalConnect;
    this.operationCost['e']['d'] = diagonalConnect;

    this.operationCost['r']['e'] = directConnect;
    this.operationCost['r']['t'] = directConnect;
    this.operationCost['r']['d'] = diagonalConnect;
    this.operationCost['r']['f'] = diagonalConnect;

    this.operationCost['t']['r'] = directConnect;
    this.operationCost['t']['y'] = directConnect;
    this.operationCost['t']['f'] = diagonalConnect;
    this.operationCost['t']['g'] = diagonalConnect;

    this.operationCost['y']['t'] = directConnect;
    this.operationCost['y']['u'] = directConnect;
    this.operationCost['y']['g'] = diagonalConnect;
    this.operationCost['y']['h'] = diagonalConnect;

    this.operationCost['u']['y'] = directConnect;
    this.operationCost['u']['i'] = directConnect;
    this.operationCost['u']['h'] = diagonalConnect;
    this.operationCost['u']['j'] = diagonalConnect;

    this.operationCost['i']['u'] = directConnect;
    this.operationCost['i']['o'] = directConnect;
    this.operationCost['i']['j'] = diagonalConnect;
    this.operationCost['i']['k'] = diagonalConnect;

    this.operationCost['o']['i'] = directConnect;
    this.operationCost['o']['p'] = directConnect;
    this.operationCost['o']['k'] = diagonalConnect;
    this.operationCost['o']['l'] = diagonalConnect;

    this.operationCost['p']['o'] = directConnect;
    this.operationCost['p']['l'] = diagonalConnect;

    this.operationCost['z']['x'] = directConnect;
    this.operationCost['z']['s'] = diagonalConnect;
    this.operationCost['z']['a'] = diagonalConnect;

    this.operationCost['x']['z'] = directConnect;
    this.operationCost['x']['c'] = directConnect;
    this.operationCost['x']['s'] = diagonalConnect;
    this.operationCost['x']['d'] = diagonalConnect;

    this.operationCost['c']['x'] = directConnect;
    this.operationCost['c']['v'] = directConnect;
    this.operationCost['c']['d'] = diagonalConnect;
    this.operationCost['c']['f'] = diagonalConnect;

    this.operationCost['v']['b'] = directConnect;
    this.operationCost['v']['c'] = directConnect;
    this.operationCost['v']['f'] = diagonalConnect;
    this.operationCost['v']['g'] = diagonalConnect;

    this.operationCost['b']['v'] = directConnect;
    this.operationCost['b']['n'] = directConnect;
    this.operationCost['b']['g'] = diagonalConnect;
    this.operationCost['b']['h'] = diagonalConnect;

    this.operationCost['n']['b'] = directConnect;
    this.operationCost['n']['m'] = directConnect;
    this.operationCost['n']['h'] = diagonalConnect;
    this.operationCost['n']['j'] = diagonalConnect;

    this.operationCost['m']['n'] = directConnect;
    this.operationCost['m']['j'] = diagonalConnect;
    this.operationCost['m']['k'] = diagonalConnect;
  }

}

