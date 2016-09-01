package eu.toolchain.datalock;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface DatalockClient {
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
   * You normally rollback a transaction in the event of d DataLockClientImpl failure.
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
      List<Mutation> mutations, Transaction transaction
  );

  /**
   * List of mutations to commit.
   *
   * @param mutations Mutations to commit.
   * @return A future containing the result of the transaction.
   */
  CompletableFuture<TransactionResult> commit(List<Mutation> mutations);

  CompletableFuture<RunQueryResponse> runQuery(Query query);

  CompletableFuture<RunQueryResponse> runQuery(Query query, ReadOptions readOptions);

  CompletableFuture<TransactionResult> commit(
      List<Mutation> mutations, Optional<Transaction> transaction
  );

  /**
   * Execute a allocate ids statement.
   *
   * @return the result of the allocate ids request.
   */
  CompletableFuture<AllocateIdsResponse> allocateIds(List<Key> keys);

  CompletableFuture<LookupResponse> lookup(List<Key> keys);

  CompletableFuture<LookupResponse> lookup(
      List<Key> keys, Optional<ReadOptions> readOptions
  );

  /**
   * High-level transactional client API.
   */
  CompletableFuture<TransactionClient> transaction();

  Key key(PathElement... elements);

  Key key(List<PathElement> elements);
}
