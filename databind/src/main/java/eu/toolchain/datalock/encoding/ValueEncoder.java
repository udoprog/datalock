package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Context;
import lombok.Data;

@Data
public class ValueEncoder extends AbstractEncoder<Value> {
  @Override
  public Value encode(final Context path, final Value instance) {
    return instance;
  }

  private static final ValueEncoder INSTANCE = new ValueEncoder();

  public static ValueEncoder get() {
    return INSTANCE;
  }
}
