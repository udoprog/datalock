package eu.toolchain.datalock;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface Entity {
  Map<String, Value> getProperties();

  com.google.datastore.v1.Entity toPb();

  KeyedEntity withKey(Key key);

  Optional<KeyedEntity> asKeyed();

  <T> T visit(Visitor<? extends T> visitor);

  static Builder builder() {
    return new Builder();
  }

  class Builder {
    private Map<String, Value> properties = new HashMap<>();

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

  interface Visitor<T> {
    T visitEmbedded(final EmbeddedEntity embedded);

    T visitKeyed(final KeyedEntity keyed);
  }

  @Data
  class EmbeddedEntity implements Entity {
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

    @Override
    public <T> T visit(final Visitor<? extends T> visitor) {
      return visitor.visitEmbedded(this);
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
  }

  @Data
  class KeyedEntity implements Entity {
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

    @Override
    public <T> T visit(final Visitor<? extends T> visitor) {
      return visitor.visitKeyed(this);
    }

    public void stuff() {
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
  }
}
