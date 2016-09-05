package eu.toolchain.datalock;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class AllocateIdsResponse {
  private final List<Key> keys;

  public static AllocateIdsResponse fromPb(
      final com.google.datastore.v1.AllocateIdsResponse pb
  ) {
    final List<Key> keys = pb.getKeysList().stream().map(Key::fromPb).collect(Collectors.toList());
    return new AllocateIdsResponse(keys);
  }
}
