package eu.toolchain.datalock;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public interface Value {
  boolean excludeFromIndexes();

  Value withExcludeFromIndexes(boolean excludeFromIndexes);

  com.google.datastore.v1.Value toPb();

  <T> T visit(Visitor<T> visitor);

  default Entity entity() {
    throw new IllegalStateException(this + ": not an entity");
  }

  static Value fromEntity(Entity entity) {
    return new EntityValue(entity);
  }

  static KeyValue fromKey(final Key value) {
    return new KeyValue(value);
  }

  static KeyValue fromKey(final Key value, final boolean excludeFromIndexes) {
    return new KeyValue(value, excludeFromIndexes);
  }

  static BlobValue fromBlob(final ByteString value) {
    return new BlobValue(value);
  }

  static BlobValue fromBlob(final ByteString value, final boolean excludeFromIndexes) {
    return new BlobValue(value, excludeFromIndexes);
  }

  static ArrayValue fromList(final List<Value> value) {
    return new ArrayValue(value);
  }

  static ArrayValue fromList(final List<Value> value, final boolean excludeFromIndexes) {
    return new ArrayValue(value, excludeFromIndexes);
  }

  static StringValue fromString(final String value) {
    return new StringValue(value);
  }

  static StringValue fromString(final String value, final boolean excludeFromIndexes) {
    return new StringValue(value, excludeFromIndexes);
  }

  static IntegerValue fromInteger(final long value) {
    return new IntegerValue(value);
  }

  static IntegerValue fromInteger(final long value, final boolean excludeFromIndexes) {
    return new IntegerValue(value, excludeFromIndexes);
  }

  static DoubleValue fromDouble(final double value) {
    return new DoubleValue(value);
  }

  static DoubleValue fromDouble(final double value, final boolean excludeFromIndexes) {
    return new DoubleValue(value, excludeFromIndexes);
  }

  static BooleanValue fromBoolean(final boolean value) {
    return new BooleanValue(value);
  }

  static BooleanValue fromBoolean(final boolean value, final boolean excludeFromIndexes) {
    return new BooleanValue(value, excludeFromIndexes);
  }

  static TimestampValue fromDate(final Date value) {
    return new TimestampValue(value.getTime() / 1000, (int) ((value.getTime() % 1000) * 1000));
  }

  static TimestampValue fromDate(final Date value, final boolean excludeFromIndexes) {
    return new TimestampValue(value.getTime() / 1000, (int) ((value.getTime() % 1000) * 1000),
        excludeFromIndexes);
  }

  static TimestampValue fromTimestamp(final long seconds, final int nanos) {
    return new TimestampValue(seconds, nanos);
  }

  static TimestampValue fromTimestamp(
      final long seconds, final int nanos, final boolean excludeFromIndexes
  ) {
    return new TimestampValue(seconds, nanos, excludeFromIndexes);
  }

  static ArrayValue fromArray(final List<Value> values) {
    return new ArrayValue(values);
  }

  static ArrayValue fromArray(
      final List<Value> values, final boolean excludeFromIndexes
  ) {
    return new ArrayValue(values, excludeFromIndexes);
  }

  static Value fromPb(final com.google.datastore.v1.Value pb) {
    final boolean excludeFromIndexes = pb.getExcludeFromIndexes();

    final com.google.datastore.v1.Value.ValueTypeCase c = pb.getValueTypeCase();

    switch (c) {
      case KEY_VALUE:
        return new KeyValue(Key.fromPb(pb.getKeyValue()), excludeFromIndexes);
      case STRING_VALUE:
        return new StringValue(pb.getStringValue(), excludeFromIndexes);
      case BLOB_VALUE:
        return new BlobValue(pb.getBlobValue(), excludeFromIndexes);
      case TIMESTAMP_VALUE:
        return TimestampValue.fromPb(pb.getTimestampValue(), excludeFromIndexes);
      case INTEGER_VALUE:
        return new IntegerValue(pb.getIntegerValue(), excludeFromIndexes);
      case DOUBLE_VALUE:
        return new DoubleValue(pb.getDoubleValue(), excludeFromIndexes);
      case BOOLEAN_VALUE:
        return new BooleanValue(pb.getBooleanValue(), excludeFromIndexes);
      case ARRAY_VALUE:
        return new ArrayValue(pb
            .getArrayValue()
            .getValuesList()
            .stream()
            .map(Value::fromPb)
            .collect(Collectors.toList()));
      case NULL_VALUE:
        return NullValue.INSTANCE;
      default:
        throw new IllegalArgumentException("Unsupported case: " + c);
    }
  }

  @Data
  class EntityValue implements Value {
    final Entity entity;
    final boolean excludeFromIndexes;

    public EntityValue(final Entity entity) {
      this(entity, false);
    }

    public EntityValue(final Entity entity, final boolean excludeFromIndexes) {
      this.entity = entity;
      this.excludeFromIndexes = excludeFromIndexes;
    }

    @Override
    public boolean excludeFromIndexes() {
      return excludeFromIndexes;
    }

    @Override
    public EntityValue withExcludeFromIndexes(final boolean excludeFromIndexes) {
      return new EntityValue(entity, excludeFromIndexes);
    }

    @Override
    public Entity entity() {
      return entity;
    }

    @Override
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitEntity(this);
    }

    @Override
    public com.google.datastore.v1.Value toPb() {
      return com.google.datastore.v1.Value
          .newBuilder()
          .setEntityValue(entity.toPb())
          .setExcludeFromIndexes(excludeFromIndexes)
          .build();
    }

    @Override
    public String toString() {
      return "<entity:" + entity.toString() + ">";
    }
  }

  @Data
  class StringValue implements Value {
    final String value;
    final boolean excludeFromIndexes;

    public StringValue(final String value) {
      this(value, false);
    }

    public StringValue(final String value, final boolean excludeFromIndexes) {
      this.value = value;
      this.excludeFromIndexes = excludeFromIndexes;
    }

    @Override
    public boolean excludeFromIndexes() {
      return excludeFromIndexes;
    }

    @Override
    public StringValue withExcludeFromIndexes(final boolean excludeFromIndexes) {
      return new StringValue(value, excludeFromIndexes);
    }

    @Override
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitString(this);
    }

    @Override
    public com.google.datastore.v1.Value toPb() {
      return com.google.datastore.v1.Value
          .newBuilder()
          .setStringValue(value)
          .setExcludeFromIndexes(excludeFromIndexes)
          .build();
    }

    @Override
    public String toString() {
      return "<string:" + value + ">";
    }
  }

  @Data
  class KeyValue implements Value {
    final Key value;
    final boolean excludeFromIndexes;

    public KeyValue(final Key value) {
      this(value, false);
    }

    public KeyValue(final Key value, final boolean excludeFromIndexes) {
      this.value = value;
      this.excludeFromIndexes = excludeFromIndexes;
    }

    @Override
    public boolean excludeFromIndexes() {
      return excludeFromIndexes;
    }

    @Override
    public KeyValue withExcludeFromIndexes(final boolean excludeFromIndexes) {
      return new KeyValue(value, excludeFromIndexes);
    }

    @Override
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitKey(this);
    }

    @Override
    public com.google.datastore.v1.Value toPb() {
      return com.google.datastore.v1.Value
          .newBuilder()
          .setKeyValue(value.toPb())
          .setExcludeFromIndexes(excludeFromIndexes)
          .build();
    }

    @Override
    public String toString() {
      return "<key:" + value + ">";
    }
  }

  @Data
  class BlobValue implements Value {
    final ByteString value;
    final boolean excludeFromIndexes;

    public BlobValue(final ByteString value) {
      this(value, false);
    }

    public BlobValue(final ByteString value, final boolean excludeFromIndexes) {
      this.value = value;
      this.excludeFromIndexes = excludeFromIndexes;
    }

    @Override
    public boolean excludeFromIndexes() {
      return excludeFromIndexes;
    }

    @Override
    public BlobValue withExcludeFromIndexes(final boolean excludeFromIndexes) {
      return new BlobValue(value, excludeFromIndexes);
    }

    @Override
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitBlob(this);
    }

    @Override
    public com.google.datastore.v1.Value toPb() {
      return com.google.datastore.v1.Value
          .newBuilder()
          .setBlobValue(value)
          .setExcludeFromIndexes(excludeFromIndexes)
          .build();
    }

    @Override
    public String toString() {
      return "<blob:" + value + ">";
    }
  }

  @Data
  class ArrayValue implements Value {
    final List<Value> values;
    final boolean excludeFromIndexes;

    public ArrayValue(final List<Value> values) {
      this(values, false);
    }

    public ArrayValue(final List<Value> values, final boolean excludeFromIndexes) {
      this.values = values;
      this.excludeFromIndexes = excludeFromIndexes;
    }

    @Override
    public boolean excludeFromIndexes() {
      return excludeFromIndexes;
    }

    @Override
    public ArrayValue withExcludeFromIndexes(final boolean excludeFromIndexes) {
      return new ArrayValue(values, excludeFromIndexes);
    }

    @Override
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitArray(this);
    }

    @Override
    public com.google.datastore.v1.Value toPb() {
      final com.google.datastore.v1.Value.Builder valueBuilder =
          com.google.datastore.v1.Value.newBuilder();
      com.google.datastore.v1.ArrayValue.Builder array =
          com.google.datastore.v1.ArrayValue.newBuilder();
      values.stream().map(Value::toPb).forEach(array::addValues);

      return valueBuilder
          .setArrayValue(array.build())
          .setExcludeFromIndexes(excludeFromIndexes)
          .build();
    }

    @Override
    public String toString() {
      return "<list:" + values + ">";
    }
  }

  @Data
  class IntegerValue implements Value {
    final long value;
    final boolean excludeFromIndexes;

    public IntegerValue(final long value) {
      this(value, false);
    }

    public IntegerValue(final long value, final boolean excludeFromIndexes) {
      this.value = value;
      this.excludeFromIndexes = excludeFromIndexes;
    }

    @Override
    public boolean excludeFromIndexes() {
      return excludeFromIndexes;
    }

    @Override
    public IntegerValue withExcludeFromIndexes(final boolean excludeFromIndexes) {
      return new IntegerValue(value, excludeFromIndexes);
    }

    @Override
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitInteger(this);
    }

    @Override
    public com.google.datastore.v1.Value toPb() {
      return com.google.datastore.v1.Value.newBuilder().setIntegerValue(value).build();
    }

    @Override
    public String toString() {
      return "<integer:" + value + ">";
    }
  }

  @Data
  class DoubleValue implements Value {
    final double value;
    final boolean excludeFromIndexes;

    public DoubleValue(final double value) {
      this(value, false);
    }

    public DoubleValue(final double value, final boolean excludeFromIndexes) {
      this.value = value;
      this.excludeFromIndexes = excludeFromIndexes;
    }

    @Override
    public boolean excludeFromIndexes() {
      return excludeFromIndexes;
    }

    @Override
    public DoubleValue withExcludeFromIndexes(final boolean excludeFromIndexes) {
      return new DoubleValue(value, excludeFromIndexes);
    }

    @Override
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitDouble(this);
    }

    @Override
    public com.google.datastore.v1.Value toPb() {
      return com.google.datastore.v1.Value.newBuilder().setDoubleValue(value).build();
    }

    @Override
    public String toString() {
      return "<double:" + value + ">";
    }
  }

  @Data
  class BooleanValue implements Value {
    final boolean value;
    final boolean excludeFromIndexes;

    public BooleanValue(final boolean value) {
      this(value, false);
    }

    public BooleanValue(final boolean value, final boolean excludeFromIndexes) {
      this.value = value;
      this.excludeFromIndexes = excludeFromIndexes;
    }

    public boolean getValue() {
      return value;
    }

    @Override
    public boolean excludeFromIndexes() {
      return excludeFromIndexes;
    }

    @Override
    public BooleanValue withExcludeFromIndexes(final boolean excludeFromIndexes) {
      return new BooleanValue(value, excludeFromIndexes);
    }

    @Override
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitBoolean(this);
    }

    @Override
    public com.google.datastore.v1.Value toPb() {
      return com.google.datastore.v1.Value.newBuilder().setBooleanValue(value).build();
    }

    @Override
    public String toString() {
      return "<boolean:" + value + ">";
    }
  }

  @Data
  class TimestampValue implements Value {
    final long seconds;
    final int nanos;
    final boolean excludeFromIndexes;

    public TimestampValue(final long seconds, final int nanos) {
      this(seconds, nanos, false);
    }

    public TimestampValue(
        final long seconds, final int nanos, final boolean excludeFromIndexes
    ) {
      this.seconds = nanos;
      this.nanos = nanos;
      this.excludeFromIndexes = excludeFromIndexes;
    }

    @Override
    public boolean excludeFromIndexes() {
      return excludeFromIndexes;
    }

    @Override
    public TimestampValue withExcludeFromIndexes(final boolean excludeFromIndexes) {
      return new TimestampValue(seconds, nanos, excludeFromIndexes);
    }

    @Override
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitTimestamp(this);
    }

    @Override
    public com.google.datastore.v1.Value toPb() {
      final Timestamp.Builder ts = Timestamp.newBuilder();
      ts.setSeconds(seconds);
      ts.setNanos(nanos);
      return com.google.datastore.v1.Value.newBuilder().setTimestampValue(ts.build()).build();
    }

    @Override
    public String toString() {
      return "<timestamp:" + seconds + ":" + nanos + ">";
    }

    public static Value fromPb(final Timestamp timestamp, final boolean excludeFromIndexes) {
      return null;
    }
  }

  @Data
  class NullValue implements Value {
    public static NullValue INSTANCE = new NullValue();

    @Override
    public boolean excludeFromIndexes() {
      return false;
    }

    @Override
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitNull(this);
    }

    @Override
    public NullValue withExcludeFromIndexes(final boolean excludeFromIndexes) {
      return this;
    }

    @Override
    public com.google.datastore.v1.Value toPb() {
      throw new RuntimeException(this + ": cannot be serialized");
    }

    @Override
    public String toString() {
      return "<nothing>";
    }
  }

  interface Visitor<T> {
    default T defaultAction(Value value) {
      throw new IllegalArgumentException("Cannot handle value: " + value);
    }

    default T visitEntity(EntityValue entity) {
      return defaultAction(entity);
    }

    default T visitString(StringValue string) {
      return defaultAction(string);
    }

    default T visitKey(KeyValue key) {
      return defaultAction(key);
    }

    default T visitBlob(BlobValue blob) {
      return defaultAction(blob);
    }

    default T visitArray(ArrayValue list) {
      return defaultAction(list);
    }

    default T visitInteger(IntegerValue integer) {
      return defaultAction(integer);
    }

    default T visitDouble(DoubleValue doubleValue) {
      return defaultAction(doubleValue);
    }

    default T visitTimestamp(TimestampValue timestamp) {
      return defaultAction(timestamp);
    }

    default T visitBoolean(BooleanValue booleanValue) {
      return defaultAction(booleanValue);
    }

    default T visitNull(NullValue nothing) {
      return defaultAction(nothing);
    }
  }
}
