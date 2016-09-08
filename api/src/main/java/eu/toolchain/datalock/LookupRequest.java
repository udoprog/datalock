package eu.toolchain.datalock;

import lombok.Data;

import java.util.List;

@Data
public class LookupRequest {
  private final List<Key> keys;
  private final ReadOptions readOptions;
}
