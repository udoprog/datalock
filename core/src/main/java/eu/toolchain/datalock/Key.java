package eu.toolchain.datalock;

import lombok.Data;

import java.util.ArrayList;
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

  public com.google.datastore.v1.Key toPb() {
    final com.google.datastore.v1.Key.Builder builder = com.google.datastore.v1.Key.newBuilder();
    path.stream().map(PathElement::toPb).forEach(builder::addPath);
    builder.setPartitionId(partitionId.toPb());
    return builder.build();
  }

  public static Key fromPb(final com.google.datastore.v1.Key pb) {
    final List<PathElement> elements = new ArrayList<>();
    pb.getPathList().stream().map(PathElement::fromPb).forEach(elements::add);
    final PartitionId partitionId = PartitionId.fromPb(pb.getPartitionId());
    return new Key(elements, partitionId);
  }
}
