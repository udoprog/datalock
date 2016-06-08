package eu.toolchain.datalock;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
public class Entity {
    private final Optional<Key> key;
    private final Map<String, Value> properties;

    public Optional<Value> get(final String name) {
        return Optional.ofNullable(properties.get(name));
    }

    public boolean isComplete() {
        return key.map(Key::isComplete).orElse(false);
    }

    public Entity withProperties(final Map<String, Value> properties) {
        return new Entity(key, properties);
    }

    public Entity withKey(final Key key) {
        return new Entity(Optional.of(key), properties);
    }

    public static Entity fromPb(final com.google.datastore.v1beta3.Entity pb) {
        final Map<String, Value> properties = new HashMap<>();

        final Optional<Key> key;

        if (pb.hasKey()) {
            key = Optional.of(Key.fromPb(pb.getKey()));
        } else {
            key = Optional.empty();
        }

        for (final Map.Entry<String, com.google.datastore.v1beta3.Value> property : pb
            .getProperties()
            .entrySet()) {
            properties.put(property.getKey(), Value.fromPb(property.getValue()));
        }

        return new Entity(key, properties);
    }

    public com.google.datastore.v1beta3.Entity toPb() {
        final com.google.datastore.v1beta3.Entity.Builder builder =
            com.google.datastore.v1beta3.Entity.newBuilder();

        key.ifPresent(k -> builder.setKey(k.toPb()));

        for (final Map.Entry<String, Value> e : properties.entrySet()) {
            builder.getMutableProperties().put(e.getKey(), e.getValue().toPb());
        }

        return builder.build();
    }

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

        public Entity build() {
            return new Entity(key, properties);
        }
    }
}
