package eu.toolchain.datalock;

import lombok.Data;

import java.util.Optional;

@Data
public class PartitionId {
  private final Optional<String> namespaceId;
  private final Optional<String> projectId;

  public com.google.datastore.v1.PartitionId toPb() {
    final com.google.datastore.v1.PartitionId.Builder builder =
        com.google.datastore.v1.PartitionId.newBuilder();
    namespaceId.ifPresent(builder::setNamespaceId);
    projectId.ifPresent(builder::setProjectId);
    return builder.build();
  }

  public static PartitionId fromPb(final com.google.datastore.v1.PartitionId pb) {
    final Optional<String> namespace = Optional.ofNullable(pb.getNamespaceId());
    final Optional<String> project = Optional.ofNullable(pb.getProjectId());

    return new PartitionId(namespace, project);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Optional<String> namespaceId = Optional.empty();
    private Optional<String> projectId = Optional.empty();

    public Builder namespaceId(final String namespaceId) {
      this.namespaceId = Optional.of(namespaceId);
      return this;
    }

    public Builder projectId(final String projectId) {
      this.projectId = Optional.of(projectId);
      return this;
    }

    public PartitionId build() {
      return new PartitionId(namespaceId, projectId);
    }
  }
}
