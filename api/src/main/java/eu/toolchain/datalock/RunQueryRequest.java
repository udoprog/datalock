package eu.toolchain.datalock;

import lombok.Data;

import java.util.Optional;

@Data
public class RunQueryRequest {
  private final Query query;
  private final Optional<ReadOptions> readOptions;
  private final Optional<PartitionId> partitionId;

  public com.google.datastore.v1.RunQueryRequest toPb() {
    final com.google.datastore.v1.RunQueryRequest.Builder builder =
        com.google.datastore.v1.RunQueryRequest.newBuilder();
    builder.setQuery(query.toPb());
    readOptions.ifPresent(r -> builder.setReadOptions(r.toPb()));
    partitionId.ifPresent(p -> builder.setPartitionId(p.toPb()));
    return builder.build();
  }
}
