package eu.toolchain.datalock;

import lombok.Data;

@Data
public class RunQueryRequest {
  private final Query query;
  private final ReadOptions readOptions;
  private final PartitionId partitionId;
}
