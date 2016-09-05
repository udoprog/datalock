package eu.toolchain.datalock;

import lombok.Data;

@Data
public class BeginTransactionResponse {
  private final Transaction transaction;

  public static BeginTransactionResponse fromPb(
      final com.google.datastore.v1.BeginTransactionResponse pb
  ) {
    return new BeginTransactionResponse(new Transaction(pb.getTransaction()));
  }
}
