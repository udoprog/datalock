package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Context;
import eu.toolchain.scribe.Decoded;
import eu.toolchain.scribe.Decoder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ListDecoder<ElementSource> extends AbstractDecoder<List<ElementSource>> {
  private final Decoder<Value, ElementSource> value;

  @Override
  public Decoded<List<ElementSource>> decode(final Context path, final Value instance) {
    return instance.visit(new Value.Visitor<Decoded<List<ElementSource>>>() {
      @Override
      public Decoded<List<ElementSource>> visitArray(final Value.ArrayValue list) {
        return Decoded.of(decodeList(path, list.getValues()));
      }

      @Override
      public Decoded<List<ElementSource>> visitNull(final Value.NullValue nothing) {
        return Decoded.absent();
      }
    });
  }

  private List<ElementSource> decodeList(final Context path, final List<Value> values) {
    final List<ElementSource> result = new ArrayList<>(values.size());

    int index = 0;

    for (final Value v : values) {
      final Context p = path.push(index++);
      value.decode(p, v).ifPresent(result::add);
    }

    return result;
  }
}
