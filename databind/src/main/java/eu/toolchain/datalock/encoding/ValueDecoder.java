package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Context;
import eu.toolchain.scribe.Decoded;
import eu.toolchain.scribe.Decoder;
import lombok.Data;

@Data
public class ValueDecoder implements Decoder<Value, Value> {
  @Override
  public Decoded<Value> decode(final Context path, final Value instance) {
    return Decoded.of(instance);
  }

  private static final ValueDecoder INSTANCE = new ValueDecoder();

  public static ValueDecoder get() {
    return INSTANCE;
  }
}
