package eu.toolchain.datalock.databind;

import eu.toolchain.datalock.Entity;
import eu.toolchain.datalock.Value;
import eu.toolchain.ogt.Context;
import eu.toolchain.ogt.EntityDecoder;
import eu.toolchain.ogt.JavaType;
import eu.toolchain.ogt.TypeDecoder;
import eu.toolchain.ogt.type.TypeMapping;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class DataLockTypeDecoder implements TypeDecoder<Value> {
    @Override
    public Object decodeBytesField(final TypeMapping typeMapping, final byte[] bytes)
        throws IOException {
        throw new RuntimeException("not supported");
    }

    @Override
    public Object decodeForeignBytesField(JavaType type, byte[] bytes) {
        throw new RuntimeException("not supported");
    }

    @Override
    public byte[] decodeBytes(Value node) {
        return node.visit(new Value.Visitor<byte[]>() {
            @Override
            public byte[] visitBlob(final Value.BlobValue blob) {
                return blob.getValue().toByteArray();
            }
        });
    }

    @Override
    public short decodeShort(Value node) {
        return node.visit(new Value.Visitor<Short>() {
            @Override
            public Short visitInteger(final Value.IntegerValue integer) {
                return (short) integer.getValue();
            }
        });
    }

    @Override
    public int decodeInteger(Value node) {
        return node.visit(new Value.Visitor<Integer>() {
            @Override
            public Integer visitInteger(final Value.IntegerValue integer) {
                return (int) integer.getValue();
            }
        });
    }

    @Override
    public long decodeLong(Value node) {
        return node.visit(new Value.Visitor<Long>() {
            @Override
            public Long visitInteger(final Value.IntegerValue integer) {
                return (long) integer.getValue();
            }
        });
    }

    @Override
    public float decodeFloat(Value node) {
        return node.visit(new Value.Visitor<Float>() {
            @Override
            public Float visitDouble(final Value.DoubleValue doubleValue) {
                return (float) doubleValue.getValue();
            }
        });
    }

    @Override
    public double decodeDouble(Value node) {
        return node.visit(new Value.Visitor<Double>() {
            @Override
            public Double visitDouble(final Value.DoubleValue doubleValue) {
                return doubleValue.getValue();
            }
        });
    }

    @Override
    public boolean decodeBoolean(Value node) {
        return node.visit(new Value.Visitor<Boolean>() {
            @Override
            public Boolean visitBoolean(final Value.BooleanValue booleanValue) {
                return booleanValue.getValue();
            }
        });
    }

    @Override
    public byte decodeByte(Value node) {
        return node.visit(new Value.Visitor<Byte>() {
            @Override
            public Byte visitBlob(final Value.BlobValue blob) {
                return blob.getValue().toByteArray()[0];
            }
        });
    }

    @Override
    public char decodeCharacter(Value node) {
        return node.visit(new Value.Visitor<Character>() {
            @Override
            public Character visitString(final Value.StringValue string) {
                return string.getValue().charAt(0);
            }
        });
    }

    @Override
    public Date decodeDate(Value node) {
        return node.visit(new Value.Visitor<Date>() {
            @Override
            public Date visitTimestamp(final Value.TimestampValue timestamp) {
                long millis = timestamp.getSeconds() * 1000;
                millis += timestamp.getNanos() / 1000;
                return new Date(millis);
            }
        });
    }

    @Override
    public List<?> decodeList(TypeMapping value, Context path, Value node) throws IOException {
        return node.visit(new Value.Visitor<List<?>>() {
            @Override
            public List<?> visitArray(final Value.ArrayValue array) {
                final List<Object> list = new ArrayList<>();

                int index = 0;

                for (final Value v : array.getValues()) {
                    list.add(value.decode(DataLockTypeDecoder.this, path.push(index++), v));
                }

                return list;
            }
        });
    }

    @Override
    public Map<?, ?> decodeMap(TypeMapping key, TypeMapping value, Context path, Value node)
        throws IOException {
        throw new IllegalArgumentException("map not supported");
    }

    @Override
    public String decodeString(Value node) {
        return node.visit(new Value.Visitor<String>() {
            @Override
            public String visitString(final Value.StringValue string) {
                return string.getValue();
            }
        });
    }

    @Override
    public EntityDecoder<Value> decodeEntity(Value value) {
        final Entity e = value.visit(new Value.Visitor<Entity>() {
            @Override
            public Entity visitEntity(final Value.EntityValue entity) {
                return entity.getEntity();
            }
        });

        return new DataLockEntityDecoder(e, this);
    }
}
