package eu.toolchain.datalock;

import lombok.Data;

import java.util.List;

@Data
public class AllocateIdsResponse {
  private final List<Key> keys;
}
