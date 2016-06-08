package eu.toolchain.datalock;

public interface ReadOptions {
    static ReadOptions fromTransaction(final Transaction transaction) {
        return new TransactionReadOptions(transaction);
    }

    static ReadOptions fromConsistency(final ReadConsistency consistency) {
        return new ConsistencyReadOptions(consistency);
    }

    com.google.datastore.v1beta3.ReadOptions toPb();
}
