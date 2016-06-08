package eu.toolchain.datalock;

import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
public class CommitRequest {
    private final List<Mutation> mutations;
    private final Optional<Transaction> transaction;
    private final Mode mode;

    public com.google.datastore.v1beta3.CommitRequest toPb() {
        final com.google.datastore.v1beta3.CommitRequest.Builder builder =
            com.google.datastore.v1beta3.CommitRequest.newBuilder();
        mutations.stream().map(Mutation::toPb).forEach(builder::addMutations);
        transaction.ifPresent(t -> builder.setTransaction(t.getBytes()));
        builder.setMode(mode.mode());
        return builder.build();
    }

    public enum Mode {
        TRANSACTIONAL(com.google.datastore.v1beta3.CommitRequest.Mode.TRANSACTIONAL),
        NON_TRANSACTIONAL(com.google.datastore.v1beta3.CommitRequest.Mode.NON_TRANSACTIONAL);

        private final com.google.datastore.v1beta3.CommitRequest.Mode mode;

        Mode(final com.google.datastore.v1beta3.CommitRequest.Mode mode) {
            this.mode = mode;
        }

        public com.google.datastore.v1beta3.CommitRequest.Mode mode() {
            return mode;
        }
    }
}
