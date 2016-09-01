package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Key;
import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Decoded;
import lombok.Data;

@Data
public class KeyDecoder extends AbstractDecoder<Key> {
  @Override
  public Decoded<Key> visitKey(final Value.KeyValue key) {
    return Decoded.of(key.getValue());
  }

  private static final KeyDecoder INSTANCE = new KeyDecoder();

  public static KeyDecoder get() {
    return INSTANCE;
  }
}
