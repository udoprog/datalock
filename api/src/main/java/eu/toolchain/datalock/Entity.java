package eu.toolchain.datalock;

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
}
