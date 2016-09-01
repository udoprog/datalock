package eu.toolchain.datalock;

import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
public class LookupRequest {
  private final List<Key> keys;
  private final Optional<ReadOptions> readOptions;
  private final String projectId;

  public com.google.datastore.v1.LookupRequest toPb() {
    final com.google.datastore.v1.LookupRequest.Builder builder =
        com.google.datastore.v1.LookupRequest.newBuilder();
    keys.stream().map(Key::toPb).forEach(builder::addKeys);
    readOptions.ifPresent(r -> builder.setReadOptions(r.toPb()));
    builder.setProjectId(projectId);
    return builder.build();
  }
}
