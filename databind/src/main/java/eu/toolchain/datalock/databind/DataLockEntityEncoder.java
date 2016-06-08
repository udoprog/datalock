package eu.toolchain.datalock.databind;

import eu.toolchain.datalock.Entity;
import eu.toolchain.datalock.Value;
import eu.toolchain.ogt.Context;
import eu.toolchain.ogt.EntityEncoder;
import eu.toolchain.ogt.binding.FieldMapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DataLockEntityEncoder implements EntityEncoder<Value> {
    final Map<String, Value> object = new HashMap<>();

    @Override
    public void setField(FieldMapping field, Context path, Object value) throws IOException {
        final Value v = field.type().encode(new DataLockTypeEncoder(), path, value);
        object.put(field.name(), v);
    }

    @Override
    public void setType(String type) throws IOException {
        object.put("type", Value.fromString(type, false));
    }

    @Override
    public Value build() {
        return Value.fromEntity(new Entity(Optional.empty(), object));
    }
}
