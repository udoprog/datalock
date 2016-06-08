package eu.toolchain.datalock.databind;

import com.google.protobuf.ByteString;
import eu.toolchain.datalock.Value;
import eu.toolchain.ogt.Context;
import eu.toolchain.ogt.EntityEncoder;
import eu.toolchain.ogt.JavaType;
import eu.toolchain.ogt.TypeEncoder;
import eu.toolchain.ogt.type.TypeMapping;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class DataLockTypeEncoder implements TypeEncoder<Value> {
    @Override
    public byte[] encodeBytesField(final TypeMapping typeMapping, final Object o)
        throws IOException {
        throw new RuntimeException("Not supported");
    }

    @Override
    public byte[] encodeForeignBytesField(JavaType type, Object value) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public Value encodeBytes(byte[] bytes) throws IOException {
        return Value.fromBlob(ByteString.copyFrom(bytes));
    }

    @Override
    public Value encodeShort(short value) throws IOException {
        return Value.fromInteger(value);
    }

    @Override
    public Value encodeInteger(int value) throws IOException {
        return Value.fromInteger(value);
    }

    @Override
    public Value encodeLong(long value) throws IOException {
        return Value.fromInteger(value);
    }

    @Override
    public Value encodeFloat(float value) throws IOException {
        return Value.fromDouble(value);
    }

    @Override
    public Value encodeDouble(double value) throws IOException {
        return Value.fromDouble(value);
    }

    @Override
    public Value encodeBoolean(boolean value) throws IOException {
        return Value.fromBoolean(value);
    }

    @Override
    public Value encodeByte(byte value) throws IOException {
        return Value.fromBlob(ByteString.copyFrom(new byte[]{value}));
    }

    @Override
    public Value encodeCharacter(char value) throws IOException {
        return Value.fromString(new String(new char[]{value}));
    }

    @Override
    public Value encodeDate(Date value) throws IOException {
        final long time = value.getTime();
        final long seconds = time / 1000;
        final int nanos = (int) (time % 1000) * 1000;
        return Value.fromTimestamp(seconds, nanos);
    }

    @Override
    public Value encodeList(TypeMapping value, List<?> list, Context path) throws IOException {
        final List<Value> values = new ArrayList<>();

        int index = 0;

        for (final Object v : list) {
            values.add(value.encode(this, path.push(index++), v));
        }

        return Value.fromList(values);
    }

    @Override
    public Value encodeMap(TypeMapping key, TypeMapping value, Map<?, ?> map, Context path)
        throws IOException {
        throw new IllegalArgumentException("Can't encode map");
    }

    @Override
    public Value encodeString(String string) throws IOException {
        return Value.fromString(string);
    }

    @Override
    public EntityEncoder<Value> newEntityEncoder() {
        return new DataLockEntityEncoder();
    }
}
