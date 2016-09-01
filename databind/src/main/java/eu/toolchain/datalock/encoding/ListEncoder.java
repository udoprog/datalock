package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Context;
import eu.toolchain.scribe.Encoder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ListEncoder<ElementSource> extends AbstractEncoder<List<ElementSource>> {
  private final Encoder<Value, ElementSource> value;

  @Override
  public Value encode(final Context path, final List<ElementSource> instance) {
    final List<Value> result = new ArrayList<>();

    int index = 0;

    for (final ElementSource value : instance) {
      final Context p = path.push(index++);
      this.value.encodeOptionally(p, value, result::add);
    }

    return Value.fromArray(result);
  }
}
