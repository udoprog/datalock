package eu.toolchain.datalock;

import java.util.List;

import lombok.Data;

@Data
public class LookupResponse {
  private final List<Key> deferred;
  private final List<Entity.KeyedEntity> found;
  private final List<Entity.KeyedEntity> missing;
}
