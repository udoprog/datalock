package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Context;
import eu.toolchain.scribe.Decoded;
import eu.toolchain.scribe.Decoder;

public abstract class AbstractDecoder<T> implements Decoder<Value, T>, Value.Visitor<Decoded<T>> {
  @Override
  public Decoded<T> visitNull(final Value.NullValue nothing) {
    return Decoded.absent();
  }

  @Override
  public Decoded<T> decode(final Context path, final Value instance) {
    return instance.visit(this);
  }
}
