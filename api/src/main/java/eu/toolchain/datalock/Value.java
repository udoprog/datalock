package eu.toolchain.datalock;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

public interface Value {
  boolean isExcludeFromIndexes();

  Value withExcludeFromIndexes(boolean excludeFromIndexes);

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

  static BlobValue fromBlob(final ByteBuffer value) {
    return new BlobValue(value);
  }

  static BlobValue fromBlob(final ByteBuffer value, final boolean excludeFromIndexes) {
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
    public StringValue withExcludeFromIndexes(final boolean excludeFromIndexes) {
      return new StringValue(value, excludeFromIndexes);
    }

    @Override
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitString(this);
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
    public KeyValue withExcludeFromIndexes(final boolean excludeFromIndexes) {
      return new KeyValue(value, excludeFromIndexes);
    }

    @Override
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitKey(this);
    }

    @Override
    public String toString() {
      return "<key:" + value + ">";
    }
  }

  @Data
  class BlobValue implements Value {
    final ByteBuffer value;
    final boolean excludeFromIndexes;

    public BlobValue(final ByteBuffer value) {
      this(value, false);
    }

    public BlobValue(final ByteBuffer value, final boolean excludeFromIndexes) {
      this.value = value;
      this.excludeFromIndexes = excludeFromIndexes;
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
    public ArrayValue withExcludeFromIndexes(final boolean excludeFromIndexes) {
      return new ArrayValue(values, excludeFromIndexes);
    }

    @Override
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitArray(this);
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
    public IntegerValue withExcludeFromIndexes(final boolean excludeFromIndexes) {
      return new IntegerValue(value, excludeFromIndexes);
    }

    @Override
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitInteger(this);
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
    public DoubleValue withExcludeFromIndexes(final boolean excludeFromIndexes) {
      return new DoubleValue(value, excludeFromIndexes);
    }

    @Override
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitDouble(this);
    }

    @Override
    public String toString() {
      return "<double:" + value + ">";
    }
  }

  @Data
  class BooleanValue implements Value {
    @Getter(AccessLevel.NONE)
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
    public BooleanValue withExcludeFromIndexes(final boolean excludeFromIndexes) {
      return new BooleanValue(value, excludeFromIndexes);
    }

    @Override
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitBoolean(this);
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
    public TimestampValue withExcludeFromIndexes(final boolean excludeFromIndexes) {
      return new TimestampValue(seconds, nanos, excludeFromIndexes);
    }

    @Override
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitTimestamp(this);
    }

    @Override
    public String toString() {
      return "<timestamp:" + seconds + ":" + nanos + ">";
    }
  }

  @Data
  class NullValue implements Value {
    public static NullValue INSTANCE = new NullValue();

    @Override
    public boolean isExcludeFromIndexes() {
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
