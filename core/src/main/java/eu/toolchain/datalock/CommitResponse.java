package eu.toolchain.datalock;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class CommitResponse implements TransactionResult {
  private final List<MutationResult> mutationResult;

  public static CommitResponse empty() {
    return new CommitResponse(Collections.emptyList());
  }

  @Override
  public List<MutationResult> mutationResult() {
    return mutationResult;
  }

  @Override
  public boolean isCommited() {
    return true;
  }
}
