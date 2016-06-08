package eu.toolchain.datalock;

import java.util.List;

public interface TransactionResult {
    List<MutationResult> mutationResult();

    boolean isCommited();
}
