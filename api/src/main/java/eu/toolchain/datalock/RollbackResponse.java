package eu.toolchain.datalock;

import java.util.Collections;
import java.util.List;

import lombok.Data;

@Data
public class RollbackResponse implements TransactionResult {
  @Override
  public List<MutationResult> mutationResult() {
    return Collections.emptyList();
  }

  @Override
  public boolean isCommited() {
    return false;
  }
}
