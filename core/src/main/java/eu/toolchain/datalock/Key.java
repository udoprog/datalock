package eu.toolchain.datalock;

import lombok.Data;

import java.util.List;

@Data
public class Key {
  private final List<PathElement> path;
  private final PartitionId partitionId;

  public Key(final List<PathElement> path, final PartitionId partitionId) {
    this.path = path;
    this.partitionId = partitionId;
  }

  public List<PathElement> path() {
    return path;
  }

  public PartitionId partitionId() {
    return partitionId;
  }

  public boolean isComplete() {
    return path.stream().allMatch(PathElement::isComplete);
  }
}
