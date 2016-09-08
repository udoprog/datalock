package eu.toolchain.datalock;

import java.util.Optional;

import lombok.Data;

@Data
public class MutationResult {
  private final Optional<Key> key;

  public static MutationResult empty() {
    return new MutationResult(Optional.empty());
  }
}
