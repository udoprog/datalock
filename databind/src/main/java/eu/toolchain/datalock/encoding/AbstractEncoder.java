package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Context;
import eu.toolchain.scribe.Encoder;

public abstract class AbstractEncoder<Source> implements Encoder<Value, Source> {
  @Override
  public Value encodeEmpty(final Context path) {
    return Value.NullValue.INSTANCE;
  }
}
