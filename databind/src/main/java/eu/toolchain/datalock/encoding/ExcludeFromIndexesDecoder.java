package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Context;
import eu.toolchain.scribe.Decoded;
import eu.toolchain.scribe.Decoder;
import lombok.Data;

@Data
public class ExcludeFromIndexesDecoder<Source> implements Decoder<Value, Source> {
  private final Decoder<Value, Source> parent;

  @Override
  public Decoded<Source> decode(final Context path, final Value instance) {
    if (!instance.excludeFromIndexes()) {
      throw path.error("attempting to decode value that is not excluded from indexes");
    }

    return parent.decode(path, instance);
  }
}
