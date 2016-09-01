package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Context;
import lombok.Data;

@Data
public class DoubleEncoder extends AbstractEncoder<Double> {
  @Override
  public Value encode(final Context path, final Double instance) {
    return Value.fromDouble(instance);
  }

  private static final DoubleEncoder INSTANCE = new DoubleEncoder();

  public static DoubleEncoder get() {
    return INSTANCE;
  }
}
