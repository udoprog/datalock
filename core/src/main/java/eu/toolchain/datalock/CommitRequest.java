package eu.toolchain.datalock;

import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
public class CommitRequest {
  private final List<Mutation> mutations;
  private final Optional<Transaction> transaction;
  private final Mode mode;

  public enum Mode {
    TRANSACTIONAL, NON_TRANSACTIONAL;
  }
}
