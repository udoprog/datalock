package eu.toolchain.datalock;

import lombok.Data;

import java.util.List;

@Data
public class AllocateIdsRequest {
  private final List<Key> keys;

  public List<Key> getKeys() {
    return keys;
  }

  public com.google.datastore.v1.AllocateIdsRequest toPb(final String projectId) {
    final com.google.datastore.v1.AllocateIdsRequest.Builder builder =
        com.google.datastore.v1.AllocateIdsRequest.newBuilder();
    builder.setProjectId(projectId);
    keys.forEach(k -> builder.addKeys(k.toPb()));
    return builder.build();
  }
}
