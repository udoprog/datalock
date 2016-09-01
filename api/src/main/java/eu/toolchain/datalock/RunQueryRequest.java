package eu.toolchain.datalock;

import com.google.datastore.v1.GqlQuery;
import lombok.Data;

@Data
public class RunQueryRequest {
  private final Query query;
  private final ReadOptions readOptions;
  private final PartitionId partitionId;

  public com.google.datastore.v1.RunQueryRequest toPb(final String projectId) {
    final com.google.datastore.v1.RunQueryRequest.Builder builder =
        com.google.datastore.v1.RunQueryRequest.newBuilder();
    builder.setProjectId(projectId);
    builder.setQuery(query.toPb());
    builder.setReadOptions(readOptions.toPb());
    builder.setPartitionId(partitionId.toPb());
    return builder.build();
  }
}
