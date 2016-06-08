package eu.toolchain.datalock;

import lombok.Data;

@Data
public class TransactionResponse {
    private final Transaction transaction;

    public static TransactionResponse fromPb(
        final com.google.datastore.v1beta3.BeginTransactionResponse pb
    ) {
        return new TransactionResponse(new Transaction(pb.getTransaction()));
    }
}
