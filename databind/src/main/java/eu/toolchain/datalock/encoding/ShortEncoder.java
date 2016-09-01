package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Context;
import lombok.Data;

@Data
public class ShortEncoder extends AbstractEncoder<Short> {
  @Override
  public Value encode(final Context path, final Short instance) {
    return Value.fromInteger(instance);
  }

  private static final ShortEncoder INSTANCE = new ShortEncoder();

  public static ShortEncoder get() {
    return INSTANCE;
  }
}
