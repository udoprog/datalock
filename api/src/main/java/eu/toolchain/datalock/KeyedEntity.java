package eu.toolchain.datalock;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
public class KeyedEntity extends Entity {
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
