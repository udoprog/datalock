package eu.toolchain.datalock;

import com.google.datastore.v1.DatastoreGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.grpc.ManagedChannel;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CoreDatalockClient implements DatalockClient {
  public static final String API_VERSION = "v1";
  public static final String USER_AGENT = CoreDatalockClient.class.getCanonicalName() + " (gzip)";

  private final Object lock = new Object();

  private final DatastoreGrpc.DatastoreStub client;
  private final PartitionId partitionId;
  private final String projectId;
  private final Optional<Credential> credential;
  private final ScheduledExecutorService scheduler;

  private final AtomicLong ref = new AtomicLong(1L);

  private volatile Future<?> refreshTask = null;
  private volatile boolean stopped = false;
  private volatile String accessToken = null;

  private final CompletableFuture<Void> stoppedFuture;

  private final Protobuf.RollbackRequestToProto rollbackRequest;
  private final Protobuf.RollbackResponseFromProto rollbackResponse;

  private final Protobuf.RunQueryRequestToProto runQueryRequest;
  private final Protobuf.RunQueryResponseFromProto runQueryResponse;

  private final Protobuf.CommitRequestToProto commitRequest;
  private final Protobuf.CommitResponseFromProto commitResponse;

  private final Protobuf.AllocateIdsRequestToProto allocateIdsRequest;
  private final Protobuf.AllocateIdsResponseFromProto allocateIdsResponse;

  private final Protobuf.LookupRequestToProto lookupRequest;
  private final Protobuf.LookupResponseFromProto lookupResponse;

  private final Protobuf.BeginTransactionRequestToProto beginTransactionRequest;
  private final Protobuf.BeginTransactionResponseFromProto beginTransactionResponse;

  private CoreDatalockClient(
      final DatastoreGrpc.DatastoreStub client, final ManagedChannel channel,
      final String namespaceId, final String projectId, final Optional<Credential> credential,
      final ScheduledExecutorService scheduler
  ) {
    this.client = client;
    this.partitionId = new PartitionId(namespaceId, projectId);
    this.projectId = projectId;
    this.credential = credential;
    this.scheduler = scheduler;

    this.stoppedFuture = new CompletableFuture<>().thenApplyAsync(c -> {
      channel.shutdown();

      try {
        channel.awaitTermination(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        channel.shutdownNow();
        throw new CompletionException(e);
      }

      // todo shutdown
      return null;
    }, scheduler);

    this.rollbackRequest = new Protobuf.RollbackRequestToProto();
    this.rollbackResponse = new Protobuf.RollbackResponseFromProto();

    this.runQueryRequest = new Protobuf.RunQueryRequestToProto(projectId);
    this.runQueryResponse = new Protobuf.RunQueryResponseFromProto();

    this.commitRequest = new Protobuf.CommitRequestToProto(projectId);
    this.commitResponse = new Protobuf.CommitResponseFromProto();

    this.allocateIdsRequest = new Protobuf.AllocateIdsRequestToProto(projectId);
    this.allocateIdsResponse = new Protobuf.AllocateIdsResponseFromProto();

    this.lookupRequest = new Protobuf.LookupRequestToProto(projectId);
    this.lookupResponse = new Protobuf.LookupResponseFromProto();

    this.beginTransactionRequest = new Protobuf.BeginTransactionRequestToProto(projectId);
    this.beginTransactionResponse = new Protobuf.BeginTransactionResponseFromProto();
  }

  @Override
  public CompletableFuture<Void> start() {
    return credential
        .map(c -> CompletableFuture.runAsync(() -> setupRefreshAccessToken(c), scheduler))
        .orElseGet(() -> CompletableFuture.completedFuture(null));
  }

  @Override
  public CompletableFuture<Void> stop() {
    synchronized (lock) {
      if (stopped) {
        return stoppedFuture;
      }

      endRefreshAccessToken();

      // remove self-reference
      decrementRef();
      stopped = true;
      return stoppedFuture;
    }
  }

  @Override
  public CompletableFuture<TransactionResult> rollback(final Transaction txn) {
    final RollbackRequest request = new RollbackRequest(txn);

    return request(DatastoreGrpc.DatastoreStub::rollback, rollbackRequest.apply(request),
        rollbackResponse.andThen(r -> new RollbackResponse()));
  }

  @Override
  public CompletableFuture<TransactionResult> commit(final List<Mutation> mutations) {
    return commit(mutations, Optional.empty());
  }

  @Override
  public CompletableFuture<RunQueryResponse> runQuery(final Query query) {
    return runQuery(query, ReadOptions.defaultInstance());
  }

  @Override
  public CompletableFuture<RunQueryResponse> runQuery(
      final Query query, final ReadOptions readOptions
  ) {
    final RunQueryRequest request = new RunQueryRequest(query, readOptions, partitionId);

    return request(DatastoreGrpc.DatastoreStub::runQuery, runQueryRequest.apply(request),
        runQueryResponse);
  }

  @Override
  public CompletableFuture<TransactionResult> commit(
      final List<Mutation> mutations, final Optional<Transaction> transaction
  ) {
    final CommitRequest request = new CommitRequest(mutations, transaction,
        transaction.isPresent() ? CommitRequest.Mode.TRANSACTIONAL
            : CommitRequest.Mode.NON_TRANSACTIONAL);

    return request(DatastoreGrpc.DatastoreStub::commit, commitRequest.apply(request),
        commitResponse);
  }

  @Override
  public CompletableFuture<AllocateIdsResponse> allocateIds(final List<Key> keys) {
    final AllocateIdsRequest request = new AllocateIdsRequest(keys);

    return request(DatastoreGrpc.DatastoreStub::allocateIds, allocateIdsRequest.apply(request),
        allocateIdsResponse);
  }

  @Override
  public CompletableFuture<LookupResponse> lookup(final List<Key> keys) {
    return lookup(keys, ReadOptions.defaultInstance());
  }

  @Override
  public CompletableFuture<LookupResponse> lookup(
      final List<Key> keys, final ReadOptions readOptions
  ) {
    final LookupRequest request = new LookupRequest(keys, readOptions);

    return request(DatastoreGrpc.DatastoreStub::lookup, lookupRequest.apply(request),
        lookupResponse);
  }

  @Override
  public CompletableFuture<TransactionClient> transaction() {
    return beginTransaction().thenApply((final Transaction txn) -> new TransactionClient() {
      @Override
      public CompletableFuture<TransactionResult> rollback() {
        return CoreDatalockClient.this.rollback(txn);
      }

      @Override
      public CompletableFuture<TransactionResult> commit(
          final List<Mutation> mutations
      ) {
        return CoreDatalockClient.this.commit(mutations, Optional.of(txn));
      }

      @Override
      public CompletableFuture<RunQueryResponse> runQuery(final Query query) {
        return CoreDatalockClient.this.runQuery(query, ReadOptions.fromTransaction(txn));
      }

      @Override
      public CompletableFuture<LookupResponse> lookup(final List<Key> keys) {
        return CoreDatalockClient.this.lookup(keys, ReadOptions.fromTransaction(txn));
      }
    });
  }

  @Override
  public Key key(final PathElement... elements) {
    return key(Stream.of(elements).collect(Collectors.toList()));
  }

  @Override
  public Key key(final List<PathElement> elements) {
    final List<PathElement> path = new ArrayList<>(elements);

    if (!path.stream().limit(path.size() - 1).allMatch(PathElement::isComplete)) {
      throw new IllegalArgumentException(
          "Only the last element in path may be incomplete: " + path);
    }

    return new Key(path, partitionId);
  }

  private void incrementRef() {
    this.ref.incrementAndGet();
  }

  private void decrementRef() {
    final long ref = this.ref.decrementAndGet();

    if (ref == 0) {
      log.info("Shutting down");
      stoppedFuture.complete(null);
    }
  }

  private void endRefreshAccessToken() {
    if (refreshTask == null) {
      return;
    }

    synchronized (lock) {
      if (refreshTask != null) {
        refreshTask.cancel(false);
        refreshTask = null;
        decrementRef();
      }
    }
  }

  private void setupRefreshAccessToken(final Credential credential) {
    // perform initial refresh
    refreshAccessToken(credential);

    // setup a schedule if it expires
    credential.getExpiresInSeconds().ifPresent(e -> {
      synchronized (lock) {
        if (stopped) {
          return;
        }

        incrementRef();

        refreshTask = scheduler.scheduleAtFixedRate(() -> {
          refreshAccessTokenIfExpired(credential);
        }, 10, 10, TimeUnit.SECONDS);
      }
    });
  }

  private void refreshAccessTokenIfExpired(final Credential credential) {
    if (stopped) {
      return;
    }

    final Optional<Long> expires = credential.getExpiresInSeconds();

    if (expires
        .map(e -> e <= 60)
        .orElseThrow(() -> new IllegalStateException("Access token does not expire"))) {
      try {
        refreshAccessToken(credential);
      } catch (final Exception e) {
        log.error("Failed to refresh soon to be expired access token", e);
      }
    }
  }

  private void refreshAccessToken(final Credential credential) {
    credential.refreshToken();

    final Optional<String> accessToken = credential.getAccessToken();

    if (!accessToken.isPresent()) {
      throw new IllegalStateException("Access token must not be null");
    }

    final String token = accessToken.get();

    // set token if none is set, or we've just refreshed.
    if (!this.accessToken.equals(token)) {
      this.accessToken = token;
    }
  }

  private <Request, ProtoResponse, Response> CompletableFuture<Response> request(
      final GrpcCall<Request, ProtoResponse> call, Request request,
      final Function<ProtoResponse, ? extends Response> transform
  ) {
    incrementRef();

    final CompletableFuture<Response> future = new CompletableFuture<>();

    final StreamObserver<ProtoResponse> observer = new StreamObserver<ProtoResponse>() {
      @Override
      public void onNext(final ProtoResponse protoResponse) {
        final Response response;

        try {
          response = transform.apply(protoResponse);
        } catch (final Exception e) {
          future.completeExceptionally(e);
          return;
        }

        future.complete(response);
      }

      @Override
      public void onError(final Throwable throwable) {
        future.completeExceptionally(throwable);
      }

      @Override
      public void onCompleted() {
        decrementRef();
      }
    };

    call.apply(client, request, observer);
    return future;
  }

  @FunctionalInterface
  interface GrpcCall<Request, Response> {
    void apply(
        DatastoreGrpc.DatastoreStub instance, Request request, StreamObserver<Response> response
    );
  }

  /**
   * Start a new transaction.
   * <p>
   * The returned {@code TransactionResult} contains the transaction if the request is
   * successful.
   *
   * @return the result of the transaction request.
   */
  private CompletableFuture<Transaction> beginTransaction() {
    final BeginTransactionRequest request = new BeginTransactionRequest();

    return request(DatastoreGrpc.DatastoreStub::beginTransaction,
        beginTransactionRequest.apply(request),
        beginTransactionResponse.andThen(BeginTransactionResponse::getTransaction));
  }

  public interface ThrowingFunction<A, B> {
    B apply(A input) throws Exception;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    public static final String DEFAULT_HOST = "datastore.googleapis.com";
    public static final int DEFAULT_PORT = 443;
    public static final String DEFAULT_PROJECT_ID = "datastore";
    public static final String DEFAULT_NAMESPACE_ID = "datastore";

    private Optional<Credential> credential = Optional.empty();
    private Optional<String> host = Optional.empty();
    private Optional<Integer> port = Optional.empty();
    private Optional<Boolean> usePlainText = Optional.empty();
    private Optional<String> projectId = Optional.empty();
    private Optional<String> namespaceId = Optional.empty();
    private Optional<ScheduledExecutorService> scheduler = Optional.empty();

    public Builder credential(final Credential credential) {
      this.credential = Optional.of(credential);
      return this;
    }

    public Builder host(final String host) {
      this.host = Optional.of(host);
      return this;
    }

    public Builder port(final int port) {
      this.port = Optional.of(port);
      return this;
    }

    public Builder usePlainText(final boolean usePlainText) {
      this.usePlainText = Optional.of(usePlainText);
      return this;
    }

    public Builder projectId(final String projectId) {
      this.projectId = Optional.of(projectId);
      return this;
    }

    public Builder namespaceId(final String namespaceId) {
      this.namespaceId = Optional.of(namespaceId);
      return this;
    }

    public Builder scheduler(final ScheduledExecutorService scheduler) {
      this.scheduler = Optional.of(scheduler);
      return this;
    }

    public CoreDatalockClient build() {
      final String host = this.host.orElse(DEFAULT_HOST);
      final int port = this.port.orElse(DEFAULT_PORT);

      final NettyChannelBuilder builder = NettyChannelBuilder.forAddress(host, port);

      builder.userAgent(USER_AGENT);
      builder.nameResolverFactory(new DnsNameResolverProvider());

      usePlainText.ifPresent(builder::usePlaintext);

      final ManagedChannel channel = builder.build();

      final DatastoreGrpc.DatastoreStub client = DatastoreGrpc.newStub(channel);

      final String namespaceId = this.namespaceId.orElse(DEFAULT_NAMESPACE_ID);
      final String projectId = this.projectId.orElse(DEFAULT_PROJECT_ID);

      final ScheduledExecutorService scheduler =
          this.scheduler.orElseGet(Executors::newSingleThreadScheduledExecutor);

      return new CoreDatalockClient(client, channel, namespaceId, projectId, credential, scheduler);
    }
  }
}
