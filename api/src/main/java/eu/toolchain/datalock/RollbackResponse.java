package eu.toolchain.datalock;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class RollbackResponse implements TransactionResult {
  public static RollbackResponse fromPb(final com.google.datastore.v1.RollbackResponse pb) {
    return new RollbackResponse();
  }

  @Override
  public List<MutationResult> mutationResult() {
    return Collections.emptyList();
  }

  @Override
  public boolean isCommited() {
    return false;
  }
}
