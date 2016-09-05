package eu.toolchain.datalock;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class Entity {
  public abstract Map<String, Value> getProperties();

  public abstract com.google.datastore.v1.Entity toPb();

  public abstract KeyedEntity withKey(final Key key);

  public abstract Optional<KeyedEntity> asKeyed();

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Optional<Key> key = Optional.empty();
    private Map<String, Value> properties = new HashMap<>();

    public Builder key(final Key key) {
      this.key = Optional.of(key);
      return this;
    }

    public Builder property(final String name, final Value value) {
      this.properties.put(name, value);
      return this;
    }

    public static Builder builder() {
      return new Builder();
    }

    public KeyedEntity build(final Key key) {
      return new KeyedEntity(key, properties);
    }

    public EmbeddedEntity build() {
      return new EmbeddedEntity(properties);
    }
  }

  @Data
  public static class EmbeddedEntity extends Entity {
    private final Map<String, Value> properties;

    public Optional<Value> get(final String name) {
      return Optional.ofNullable(properties.get(name));
    }

    public EmbeddedEntity withProperties(final Map<String, Value> properties) {
      return new EmbeddedEntity(properties);
    }

    @Override
    public KeyedEntity withKey(final Key key) {
      return new KeyedEntity(key, properties);
    }

    @Override
    public Optional<KeyedEntity> asKeyed() {
      return Optional.empty();
    }

    public static EmbeddedEntity fromPb(final com.google.datastore.v1.Entity pb) {
      final Map<String, Value> properties = new HashMap<>();

      final Optional<Key> key;

      if (pb.hasKey()) {
        throw new IllegalStateException("Embedded entities do not have keys");
      }

      for (final Map.Entry<String, com.google.datastore.v1.Value> property : pb
          .getProperties()
          .entrySet()) {
        properties.put(property.getKey(), Value.fromPb(property.getValue()));
      }

      return new EmbeddedEntity(properties);
    }

    public com.google.datastore.v1.Entity toPb() {
      final com.google.datastore.v1.Entity.Builder builder =
          com.google.datastore.v1.Entity.newBuilder();

      for (final Map.Entry<String, Value> e : properties.entrySet()) {
        builder.getMutableProperties().put(e.getKey(), e.getValue().toPb());
      }

      return builder.build();
    }
  }

  @Data
  public static class KeyedEntity extends Entity {
    private final Key key;
    private final Map<String, Value> properties;

    public Optional<Value> get(final String name) {
      return Optional.ofNullable(properties.get(name));
    }

    public boolean isComplete() {
      return key.isComplete();
    }

    public KeyedEntity withProperties(final Map<String, Value> properties) {
      return new KeyedEntity(key, properties);
    }

    @Override
    public KeyedEntity withKey(final Key key) {
      return new KeyedEntity(key, properties);
    }

    @Override
    public Optional<KeyedEntity> asKeyed() {
      return Optional.of(this);
    }

    public static KeyedEntity fromPb(final com.google.datastore.v1.Entity pb) {
      final Map<String, Value> properties = new HashMap<>();

      if (!pb.hasKey()) {
        throw new IllegalStateException("Expected key");
      }

      final Key key = Key.fromPb(pb.getKey());

      for (final Map.Entry<String, com.google.datastore.v1.Value> property : pb
          .getProperties()
          .entrySet()) {
        properties.put(property.getKey(), Value.fromPb(property.getValue()));
      }

      return new KeyedEntity(key, properties);
    }

    public com.google.datastore.v1.Entity toPb() {
      final com.google.datastore.v1.Entity.Builder builder =
          com.google.datastore.v1.Entity.newBuilder();

      builder.setKey(key.toPb());

      for (final Map.Entry<String, Value> e : properties.entrySet()) {
        builder.getMutableProperties().put(e.getKey(), e.getValue().toPb());
      }

      return builder.build();
    }
  }
}
