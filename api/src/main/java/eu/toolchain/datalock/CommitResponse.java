package eu.toolchain.datalock;

import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CommitResponse implements TransactionResult {
    private final List<MutationResult> mutationResult;

    public static CommitResponse empty() {
        return new CommitResponse(Collections.emptyList());
    }

    public static CommitResponse fromPb(final com.google.datastore.v1beta3.CommitResponse pb) {
        final List<MutationResult> mutationResult = pb
            .getMutationResultsList()
            .stream()
            .map(MutationResult::fromPb)
            .collect(Collectors.toList());

        return new CommitResponse(mutationResult);
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
