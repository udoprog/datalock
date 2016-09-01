package eu.toolchain.datalock;

import lombok.Data;

@Data
public class RollbackRequest {
  private final Transaction transaction;

  public com.google.datastore.v1.RollbackRequest toPb() {
    final com.google.datastore.v1.RollbackRequest.Builder builder =
        com.google.datastore.v1.RollbackRequest.newBuilder();
    builder.setTransaction(transaction.getBytes());
    return builder.build();
  }
}
