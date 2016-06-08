package eu.toolchain.datalock;

import lombok.Data;

import java.util.List;

@Data
public class AllocateIdsRequest {
    private final List<Key> keys;

    public List<Key> getKeys() {
        return keys;
    }

    public com.google.datastore.v1beta3.AllocateIdsResponse toPb() {
        final com.google.datastore.v1beta3.AllocateIdsResponse.Builder builder =
            com.google.datastore.v1beta3.AllocateIdsResponse.newBuilder();
        keys.stream().forEach(k -> builder.addKeys(k.toPb()));
        return builder.build();
    }
}
