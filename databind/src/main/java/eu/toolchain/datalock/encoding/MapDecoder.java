package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Context;
import eu.toolchain.scribe.Decoded;
import eu.toolchain.scribe.Decoder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class MapDecoder<ValueSource> implements Decoder<Value, Map<String, ValueSource>> {
  private final Decoder<Value, ValueSource> value;

  @Override
  public Decoded<Map<String, ValueSource>> decode(
      final Context path, final Value instance
  ) {
    return instance.visit(new Value.Visitor<Decoded<Map<String, ValueSource>>>() {
      @Override
      public Decoded<Map<String, ValueSource>> visitEntity(final Value.EntityValue value) {
        return Decoded.of(decodeMap(path, value.getEntity().getProperties()));
      }

      @Override
      public Decoded<Map<String, ValueSource>> visitNull(final Value.NullValue nothing) {
        return Decoded.absent();
      }

      @Override
      public Decoded<Map<String, ValueSource>> defaultAction(final Value value) {
        throw path.error("expected entity");
      }
    });
  }

  private Map<String, ValueSource> decodeMap(final Context path, final Map<String, Value> values) {
    final Map<String, ValueSource> result = new HashMap<>(values.size());

    for (final Map.Entry<String, Value> e : values.entrySet()) {
      final Context p = path.push(e.getKey());
      value.decode(p, e.getValue()).ifPresent(v -> result.put(e.getKey(), v));
    }

    return result;
  }
}
