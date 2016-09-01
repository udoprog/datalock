package eu.toolchain.datalock;

import com.google.datastore.v1.EntityResult;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class LookupResponse {
  private final List<Key> deferred;
  private final List<KeyedEntity> found;
  private final List<KeyedEntity> missing;

  public static LookupResponse fromPb(final com.google.datastore.v1.LookupResponse pb) {
    final List<Key> deferred =
        pb.getDeferredList().stream().map(Key::fromPb).collect(Collectors.toList());

    final List<KeyedEntity> found = pb
        .getFoundList()
        .stream()
        .filter(EntityResult::hasEntity)
        .map(EntityResult::getEntity)
        .map(KeyedEntity::fromPb)
        .collect(Collectors.toList());

    final List<KeyedEntity> missing = pb
        .getMissingList()
        .stream()
        .filter(EntityResult::hasEntity)
        .map(EntityResult::getEntity)
        .map(KeyedEntity::fromPb)
        .collect(Collectors.toList());

    return new LookupResponse(deferred, found, missing);
  }
}
