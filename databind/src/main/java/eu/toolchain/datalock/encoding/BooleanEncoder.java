package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Context;
import lombok.Data;

@Data
public class BooleanEncoder extends AbstractEncoder<Boolean> {
  @Override
  public Value encode(final Context path, final Boolean instance) {
    return Value.fromBoolean(instance);
  }

  private static final BooleanEncoder INSTANCE = new BooleanEncoder();

  public static BooleanEncoder get() {
    return INSTANCE;
  }
}
