
package io.github.mightguy.spellcheck.symspell.common;

import com.sangupta.murmur.Murmur3;
import io.github.mightguy.spellcheck.symspell.api.HashFunction;

public class Murmur3HashFunction implements HashFunction {

  private static final long SEED = 0x7f3a21eaL;

  public long hash(byte[] bytes) {
    return Murmur3.hash_x86_32(bytes, bytes.length, SEED);
  }

  public Long hash(String data) {
    return hash(data.getBytes());
  }
}
