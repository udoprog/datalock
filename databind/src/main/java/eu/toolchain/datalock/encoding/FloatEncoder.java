package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Context;
import lombok.Data;

@Data
public class FloatEncoder extends AbstractEncoder<Float> {
  @Override
  public Value encode(final Context path, final Float instance) {
    return Value.fromDouble(instance);
  }

  private static final FloatEncoder INSTANCE = new FloatEncoder();

  public static FloatEncoder get() {
    return INSTANCE;
  }
}
