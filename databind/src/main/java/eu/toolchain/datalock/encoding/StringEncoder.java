package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Context;
import lombok.Data;

@Data
public class StringEncoder extends AbstractEncoder<String> {
  @Override
  public Value encode(final Context path, final String instance) {
    return Value.fromString(instance);
  }

  private static final StringEncoder INSTANCE = new StringEncoder();

  public static StringEncoder get() {
    return INSTANCE;
  }
}
