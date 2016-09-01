package eu.toolchain.datalock;

import lombok.Data;

@Data
public class PartitionId {
  private final String namespaceId;
  private final String projectId;

  public com.google.datastore.v1.PartitionId toPb() {
    final com.google.datastore.v1.PartitionId.Builder builder =
        com.google.datastore.v1.PartitionId.newBuilder();
    builder.setNamespaceId(namespaceId);
    builder.setProjectId(projectId);
    return builder.build();
  }

  public static PartitionId fromPb(final com.google.datastore.v1.PartitionId pb) {
    final String namespaceId = pb.getNamespaceId();
    final String projectId = pb.getProjectId();

    return new PartitionId(namespaceId, projectId);
  }
}
