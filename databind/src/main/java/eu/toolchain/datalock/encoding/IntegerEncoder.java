package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Context;
import lombok.Data;

@Data
public class IntegerEncoder extends AbstractEncoder<Integer> {
  @Override
  public Value encode(final Context path, final Integer instance) {
    return Value.fromInteger(instance);
  }

  private static final IntegerEncoder INSTANCE = new IntegerEncoder();

  public static IntegerEncoder get() {
    return INSTANCE;
  }
}
