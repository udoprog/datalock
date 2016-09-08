package eu.toolchain.datalock;

import lombok.Data;

@Data
public class PartitionId {
  private final String namespaceId;
  private final String projectId;
}
