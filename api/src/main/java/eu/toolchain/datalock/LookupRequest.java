package eu.toolchain.datalock;

import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
public class LookupRequest {
  private final List<Key> keys;
  private final Optional<ReadOptions> readOptions;

  public com.google.datastore.v1.LookupRequest toPb(final String projectId) {
    final com.google.datastore.v1.LookupRequest.Builder builder =
        com.google.datastore.v1.LookupRequest.newBuilder();
    builder.setProjectId(projectId);
    keys.stream().map(Key::toPb).forEach(builder::addKeys);
    readOptions.ifPresent(r -> builder.setReadOptions(r.toPb()));
    return builder.build();
  }
}
