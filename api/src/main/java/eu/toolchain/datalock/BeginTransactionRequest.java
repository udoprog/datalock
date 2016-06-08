package eu.toolchain.datalock;

import lombok.Data;

@Data
public class BeginTransactionRequest {
    public com.google.datastore.v1beta3.BeginTransactionRequest toPb() {
        final com.google.datastore.v1beta3.BeginTransactionRequest.Builder builder =
            com.google.datastore.v1beta3.BeginTransactionRequest.newBuilder();
        return builder.build();
    }
}
