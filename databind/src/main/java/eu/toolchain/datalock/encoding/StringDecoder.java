package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Decoded;
import lombok.Data;

@Data
public class StringDecoder extends AbstractDecoder<String> {
  @Override
  public Decoded<String> visitString(final Value.StringValue value) {
    return Decoded.of(value.getValue());
  }

  private static final StringDecoder INSTANCE = new StringDecoder();

  public static StringDecoder get() {
    return INSTANCE;
  }
}
