package eu.toolchain.datalock;

import java.util.concurrent.CompletableFuture;

public interface TransactionClient extends Client {
  CompletableFuture<TransactionResult> rollback();
}
