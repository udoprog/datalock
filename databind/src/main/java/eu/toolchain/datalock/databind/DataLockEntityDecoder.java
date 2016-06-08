package eu.toolchain.datalock.databind;

import eu.toolchain.datalock.Entity;
import eu.toolchain.datalock.Value;
import eu.toolchain.ogt.Context;
import eu.toolchain.ogt.EntityDecoder;
import eu.toolchain.ogt.binding.FieldMapping;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class DataLockEntityDecoder implements EntityDecoder<Value> {
    private final Entity entity;
    private final DataLockTypeDecoder decoder;

    @Override
    public Optional<String> decodeType() {
        return entity.get("type").map(v -> v.visit(new Value.Visitor<String>() {
            @Override
            public String visitString(final Value.StringValue string) {
                return string.getValue();
            }

            @Override
            public String defaultAction(final Value value) {
                throw new IllegalArgumentException("Expected string, but got: " + value);
            }
        }));
    }

    @Override
    public Optional<Object> decodeField(FieldMapping field, Context path) {
        return entity.get(field.name()).map(n -> field.type().decode(decoder, path, n));
    }
}
