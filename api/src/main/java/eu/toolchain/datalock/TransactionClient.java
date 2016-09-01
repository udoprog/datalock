package eu.toolchain.datalock;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TransactionClient {
  CompletableFuture<TransactionResult> rollback();

  CompletableFuture<TransactionResult> commit(List<Mutation> mutations);

  CompletableFuture<RunQueryResponse> runQuery(Query query);

  CompletableFuture<LookupResponse> lookup(List<Key> keys);
}
