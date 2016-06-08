package eu.toolchain.datalock;

import lombok.Data;

@Data
public class TransactionReadOptions implements ReadOptions {
    private final Transaction transaction;

    public TransactionReadOptions(final Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public com.google.datastore.v1beta3.ReadOptions toPb() {
        final com.google.datastore.v1beta3.ReadOptions.Builder builder = com.google.datastore.v1beta3.ReadOptions.newBuilder();
        builder.setTransaction(transaction.getBytes());
        return builder.build();
    }
}
