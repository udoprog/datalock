package eu.toolchain.datalock;

import lombok.Data;

import java.util.List;

@Data
public class AllocateIdsRequest {
  private final List<Key> keys;

  public List<Key> getKeys() {
    return keys;
  }
}
