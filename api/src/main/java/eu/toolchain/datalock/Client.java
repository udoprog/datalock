package eu.toolchain.datalock;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface Client {
  CompletableFuture<TransactionResult> commit(List<Mutation> mutations);

  CompletableFuture<RunQueryResponse> runQuery(Query query);

  CompletableFuture<LookupResponse> lookup(List<Key> keys);

  /**
   * Lookup a single key.
   */
  default CompletableFuture<Optional<Entity.KeyedEntity>> lookupOne(Key key) {
    return lookup(Collections.singletonList(key)).thenApply(results -> {
      return results.getFound().stream().findFirst();
    });
  }
}
