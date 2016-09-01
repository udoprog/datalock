package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Context;
import lombok.Data;

@Data
public class LongEncoder extends AbstractEncoder<Long> {
  @Override
  public Value encode(final Context path, final Long instance) {
    return Value.fromInteger(instance);
  }

  private static final LongEncoder INSTANCE = new LongEncoder();

  public static LongEncoder get() {
    return INSTANCE;
  }
}
