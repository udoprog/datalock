package eu.toolchain.datalock;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface DatalockClient extends Client {
  /**
   * Start the client.
   *
   * @return A future when completed indicate that the client is ready to use.
   */
  CompletableFuture<Void> start();

  /**
   * Stop the client.
   *
   * @return A future when completed indicate that the client has been stopped.
   */
  CompletableFuture<Void> stop();

  /**
   * Rollback a given transaction.
   * <p>
   * You normally rollback a transaction in the event of d CoreDatalockClient failure.
   *
   * @param txn the transaction.
   * @return the result of the rollback request.
   */
  CompletableFuture<TransactionResult> rollback(Transaction txn);

  /**
   * Commit the given mutation under the given transaction.
   *
   * @param mutations Mutations to commit.
   * @param transaction Transaction to commit request under.
   * @return A commit response.
   */
  CompletableFuture<TransactionResult> commit(
      List<Mutation> mutations, Optional<Transaction> transaction
  );

  CompletableFuture<RunQueryResponse> runQuery(Query query, ReadOptions readOptions);

  /**
   * Execute a allocate ids statement.
   *
   * @return the result of the allocate ids request.
   */
  CompletableFuture<AllocateIdsResponse> allocateIds(List<Key> keys);

  CompletableFuture<LookupResponse> lookup(List<Key> keys, ReadOptions readOptions);

  /**
   * High-level transactional client API.
   */
  CompletableFuture<TransactionClient> transaction();

  Key key(PathElement... elements);

  Key key(List<PathElement> elements);
}
