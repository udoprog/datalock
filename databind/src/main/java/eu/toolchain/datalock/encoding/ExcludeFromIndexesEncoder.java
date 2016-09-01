package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Context;
import eu.toolchain.scribe.Encoder;
import lombok.Data;

@Data
public class ExcludeFromIndexesEncoder<Source> implements Encoder<Value, Source> {
  private final Encoder<Value, Source> parent;

  @Override
  public Value encode(final Context path, final Source instance) {
    return parent.encode(path, instance).withExcludeFromIndexes(true);
  }

  @Override
  public Value encodeEmpty(final Context path) {
    return parent.encodeEmpty(path).withExcludeFromIndexes(true);
  }
}
