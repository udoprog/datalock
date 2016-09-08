package eu.toolchain.datalock;

import lombok.Data;

@Data
public class RollbackRequest {
  private final Transaction transaction;
}
