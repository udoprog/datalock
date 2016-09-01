package eu.toolchain.datalock;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
public class EmbeddedEntity extends Entity {
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
