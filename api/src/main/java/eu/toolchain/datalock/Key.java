package eu.toolchain.datalock;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class Key {
  private final List<PathElement> path;
  private final Optional<PartitionId> partitionId;

  public Key(final List<PathElement> path, final Optional<PartitionId> partitionId) {
    this.path = path;
    this.partitionId = partitionId;
  }

  public List<PathElement> path() {
    return path;
  }

  public Optional<PartitionId> partitionId() {
    return partitionId;
  }

  public boolean isComplete() {
    return path.stream().allMatch(PathElement::isComplete);
  }

  public com.google.datastore.v1.Key toPb() {
    final com.google.datastore.v1.Key.Builder builder = com.google.datastore.v1.Key.newBuilder();
    path.stream().map(PathElement::toPb).forEach(builder::addPath);
    partitionId.map(PartitionId::toPb).ifPresent(builder::setPartitionId);
    return builder.build();
  }

  public static Key fromPb(final com.google.datastore.v1.Key pb) {
    final List<PathElement> elements = new ArrayList<>();
    pb.getPathList().stream().map(PathElement::fromPb).forEach(elements::add);
    final Optional<PartitionId> partitionId =
        pb.hasPartitionId() ? Optional.of(PartitionId.fromPb(pb.getPartitionId()))
            : Optional.empty();
    return new Key(elements, partitionId);
  }
}
