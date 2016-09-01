package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Entity;
import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Context;
import eu.toolchain.scribe.Encoder;
import lombok.Data;

import java.util.Map;

@Data
public class MapEncoder<ValueSource> extends AbstractEncoder<Map<String, ValueSource>> {
  private final Encoder<Value, ValueSource> value;

  @Override
  public Value encode(final Context path, final Map<String, ValueSource> instance) {
    final Entity.Builder entity = Entity.builder();

    for (final Map.Entry<String, ValueSource> e : instance.entrySet()) {
      final Context p = path.push(e.getKey());

      value.encodeOptionally(p, e.getValue(), target -> {
        entity.property(e.getKey(), target);
      });
    }

    return Value.fromEntity(entity.build());
  }
}
