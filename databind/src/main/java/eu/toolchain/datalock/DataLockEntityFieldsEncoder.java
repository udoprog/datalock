package eu.toolchain.datalock;

import eu.toolchain.scribe.Context;
import eu.toolchain.scribe.EntityFieldEncoder;
import eu.toolchain.scribe.EntityFieldsEncoder;
import eu.toolchain.scribe.Flags;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DataLockEntityFieldsEncoder implements EntityFieldsEncoder<Value> {
  final Entity.Builder object = Entity.builder();

  private static final Value.Visitor<Key> TO_KEY = new Value.Visitor<Key>() {
    @Override
    public Key visitKey(final Value.KeyValue key) {
      return key.getValue();
    }
  };

  @Override
  public <Source> void encodeField(
      EntityFieldEncoder<Value, Source> field, Context path, Source value
  ) {
    // TODO: implement infrastructure to make encoders aware of field annotations to avoid the
    // runtime and complexity penalty of checking them here.

    final Flags flags = field.getFlags();

    if (flags.getFlag(DataLockFlags.KeyFlag.class).findFirst().isPresent()) {
      field.encodeOptionally(path.push(field.getName()), value, target -> {
        object.key(target.visit(TO_KEY));
      });
    } else {
      field.encodeOptionally(path.push(field.getName()), value, target -> {
        object.property(field.getName(), target);
      });
    }
  }

  @Override
  public Value buildEmpty(final Context path) {
    return Value.NullValue.INSTANCE;
  }

  @Override
  public Value build() {
    return Value.fromEntity(object.build());
  }
}
