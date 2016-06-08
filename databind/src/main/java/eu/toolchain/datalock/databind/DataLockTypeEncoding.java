package eu.toolchain.datalock.databind;

import eu.toolchain.datalock.Entity;
import eu.toolchain.datalock.Value;
import eu.toolchain.ogt.TypeEncoding;

public class DataLockTypeEncoding<T> {
    private static final Value.Visitor<Entity> TO_ENTITY = new Value.Visitor<Entity>() {
        @Override
        public Entity visitEntity(final Value.EntityValue entity) {
            return entity.getEntity();
        }
    };

    final TypeEncoding<T, Value> encoding;

    public DataLockTypeEncoding(final TypeEncoding<T, Value> encoding) {
        this.encoding = encoding;
    }

    public T decodeValue(final Value value) {
        return encoding.decode(value);
    }

    public T decodeEntity(final Entity entity) {
        return encoding.decode(Value.fromEntity(entity));
    }

    public Value encodeValue(final T value) {
        return encoding.encode(value);
    }

    public Entity encodeEntity(final T value) {
        return encoding.encode(value).visit(TO_ENTITY);
    }
}
