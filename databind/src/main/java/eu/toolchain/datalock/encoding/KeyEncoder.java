package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Key;
import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Context;
import lombok.Data;

@Data
public class KeyEncoder extends AbstractEncoder<Key> {
  @Override
  public Value encode(final Context path, final Key instance) {
    return Value.fromKey(instance);
  }

  private static final KeyEncoder INSTANCE = new KeyEncoder();

  public static KeyEncoder get() {
    return INSTANCE;
  }
}
