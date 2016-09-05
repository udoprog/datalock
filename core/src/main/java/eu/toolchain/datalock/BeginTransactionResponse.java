package eu.toolchain.datalock;

import lombok.Data;

@Data
public class BeginTransactionResponse {
  private final Transaction transaction;
}
