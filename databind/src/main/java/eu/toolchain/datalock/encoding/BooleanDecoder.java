package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Decoded;
import lombok.Data;

@Data
public class BooleanDecoder extends AbstractDecoder<Boolean> {
  @Override
  public Decoded<Boolean> visitBoolean(final Value.BooleanValue value) {
    return Decoded.of(value.getValue());
  }

  private static final BooleanDecoder INSTANCE = new BooleanDecoder();

  public static BooleanDecoder get() {
    return INSTANCE;
  }
}
