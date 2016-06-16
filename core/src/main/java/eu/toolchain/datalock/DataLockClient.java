package eu.toolchain.datalock;

import com.google.api.client.auth.oauth2.Credential;
import com.google.protobuf.MessageLite;
import com.google.rpc.Status;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

@Slf4j
public class DataLockClient {
    public static final MediaType X_PROTOBUF = MediaType.parse("application/x-protobuf");
    public static final String API_VERSION = "v1beta3";
    public static final String USER_AGENT = DataLockClient.class.getCanonicalName() + " (gzip)";

    private final Object lock = new Object();

    private final Call.Factory client;
    private final String url;
    private final Optional<PartitionId> partitionId;
    private final String projectId;
    private final Optional<Credential> credential;
    private final ScheduledExecutorService scheduler;

    private final AtomicLong ref = new AtomicLong(1L);

    private volatile Future<?> refreshTask = null;
    private volatile boolean stopped = false;
    private volatile String accessToken = null;

    private final CompletableFuture<Void> stoppedFuture;

    private DataLockClient(
        final OkHttpClient client, final String url, final String projectId,
        final Optional<String> namespaceId, final Optional<Credential> credential,
        final ScheduledExecutorService scheduler
    ) {
        this.client = client;
        this.url = String.format("%s/%s/projects/%s", url, API_VERSION, projectId);
        this.partitionId = Optional.of(buildPartitionId(namespaceId, projectId));
        this.projectId = projectId;
        this.credential = credential;
        this.scheduler = scheduler;

        this.stoppedFuture = new CompletableFuture<>().thenApplyAsync(c -> {
            client.connectionPool().evictAll();
            return null;
        }, scheduler);
    }

    private PartitionId buildPartitionId(
        final Optional<String> namespaceId, final String projectId
    ) {
        final PartitionId.Builder builder = PartitionId.builder();
        builder.projectId(projectId);
        namespaceId.ifPresent(builder::namespaceId);
        return builder.build();
    }

    public CompletableFuture<Void> start() {
        return credential
            .map(c -> CompletableFuture.runAsync(() -> setupRefreshAccessToken(c), scheduler))
            .orElseGet(() -> CompletableFuture.completedFuture(null));
    }

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
        Optional.ofNullable(credential.getExpiresInSeconds()).ifPresent(e -> {
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

        final Optional<Long> expires = Optional.ofNullable(credential.getExpiresInSeconds());

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
        try {
            credential.refreshToken();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        final String accessToken = credential.getAccessToken();

        if (accessToken == null) {
            throw new IllegalStateException("Access token must not be null");
        }

        // set token if none is set, or we've just refreshed.
        if (this.accessToken == null || !this.accessToken.equals(accessToken)) {
            this.accessToken = accessToken;
        }
    }

    private Call request(final String method, final MessageLite payload) {
        final Request.Builder builder = new Request.Builder();
        builder.url(url + ":" + method);
        builder.method("POST", RequestBody.create(X_PROTOBUF, payload.toByteArray()));

        if (accessToken != null) {
            builder.addHeader("Authorization", "Bearer " + accessToken);
        }

        builder.addHeader("User-Agent", USER_AGENT);
        builder.addHeader("Accept-Encoding", "gzip");

        return client.newCall(builder.build());
    }

    private InputStream readResponseBody(final Response r) throws IOException {
        if ("gzip".equals(r.header("content-encoding"))) {
            return new GZIPInputStream(r.body().byteStream());
        }

        return r.body().byteStream();
    }

    private <T, R> CompletableFuture<R> request(
        final String endpoint, final MessageLite content,
        ThrowingFunction<InputStream, T> fromResponse, Function<T, R> fromPb
    ) {
        final CompletableFuture<Response> response = new CompletableFuture<>();

        request(endpoint, content).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                response.completeExceptionally(e);
            }

            @Override
            public void onResponse(final Call call, final Response r) throws IOException {
                response.complete(r);
            }
        });

        incrementRef();

        return response.whenComplete((r, e) -> decrementRef()).thenApply((Response r) -> {
            if (!r.isSuccessful()) {
                final MediaType contentType = MediaType.parse(r.header("content-type"));

                final InputStream input;

                try {
                    input = readResponseBody(r);
                } catch (IOException e) {
                    throw new DatastoreException(r.code(), "Failed to read response", e);
                }

                if (X_PROTOBUF.equals(contentType)) {
                    final Status s;

                    try {
                        s = Status.parseFrom(input);
                    } catch (IOException e) {
                        throw new DatastoreException(r.code(), "Failed to decode Status", e);
                    }

                    throw new DatastoreException(r.code(), s.getMessage());
                }

                try {
                    throw new DatastoreException(r.code(), r.body().string());
                } catch (IOException e) {
                    throw new DatastoreException(r.code(), "Failed to decode body as String", e);
                }
            }

            try {
                return fromPb.apply(fromResponse.apply(r.body().byteStream()));
            } catch (final Exception e) {
                throw new DatastoreException(e);
            }
        });
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

        return request("beginTransaction", request.toPb(),
            com.google.datastore.v1beta3.BeginTransactionResponse::parseFrom,
            TransactionResponse::fromPb).thenApply(TransactionResponse::getTransaction);
    }

    /**
     * Rollback a given transaction.
     * <p>
     * You normally rollback a transaction in the event of d DataLockClient failure.
     *
     * @param txn the transaction.
     * @return the result of the rollback request.
     */
    public CompletableFuture<TransactionResult> rollback(final Transaction txn) {
        final RollbackRequest request = new RollbackRequest(txn);

        return request("rollback", request.toPb(),
            com.google.datastore.v1beta3.RollbackResponse::parseFrom, RollbackResponse::fromPb);
    }

    /**
     * Commit the given mutation under the given transaction.
     *
     * @param transaction Transaction to commit request under.
     * @return A commit response.
     */
    public CompletableFuture<TransactionResult> commit(
        final List<Mutation> mutations, final Transaction transaction
    ) {
        return commit(mutations, Optional.of(transaction));
    }

    public CompletableFuture<TransactionResult> commit(final List<Mutation> mutations) {
        return commit(mutations, Optional.empty());
    }

    public CompletableFuture<RunQueryResponse> runQuery(final Query query) {
        return runQuery(query, Optional.empty());
    }

    public CompletableFuture<RunQueryResponse> runQuery(
        final Query query, final ReadOptions readOptions
    ) {
        return runQuery(query, Optional.of(readOptions));
    }

    private CompletableFuture<RunQueryResponse> runQuery(
        final Query query, final Optional<ReadOptions> readOptions
    ) {
        final RunQueryRequest request = new RunQueryRequest(query, readOptions, partitionId);

        return request("runQuery", request.toPb(),
            com.google.datastore.v1beta3.RunQueryResponse::parseFrom, RunQueryResponse::fromPb);
    }

    private CompletableFuture<TransactionResult> commit(
        final List<Mutation> mutations, final Optional<Transaction> transaction
    ) {
        final CommitRequest request = new CommitRequest(mutations, transaction,
            transaction.isPresent() ? CommitRequest.Mode.TRANSACTIONAL
                : CommitRequest.Mode.NON_TRANSACTIONAL);

        return request("commit", request.toPb(),
            com.google.datastore.v1beta3.CommitResponse::parseFrom, CommitResponse::fromPb);
    }

    /**
     * Execute a allocate ids statement.
     *
     * @return the result of the allocate ids request.
     */
    public CompletableFuture<AllocateIdsResponse> allocateIds(final List<Key> keys) {
        final AllocateIdsRequest request = new AllocateIdsRequest(keys);

        return request("allocateIds", request.toPb(),
            com.google.datastore.v1beta3.AllocateIdsResponse::parseFrom,
            AllocateIdsResponse::fromPb);
    }

    public CompletableFuture<LookupResponse> lookup(final List<Key> keys) {
        return lookup(keys, Optional.empty());
    }

    public CompletableFuture<LookupResponse> lookup(
        final List<Key> keys, final Optional<ReadOptions> readOptions
    ) {
        final LookupRequest request = new LookupRequest(keys, readOptions, projectId);

        return request("lookup", request.toPb(),
            com.google.datastore.v1beta3.LookupResponse::parseFrom, LookupResponse::fromPb);
    }

    /**
     * High-level transactional client API.
     */
    public CompletableFuture<TransactionClient> transaction() {
        return beginTransaction().thenApply((final Transaction txn) -> new TransactionClient() {
            @Override
            public CompletableFuture<TransactionResult> rollback() {
                return DataLockClient.this.rollback(txn);
            }

            @Override
            public CompletableFuture<TransactionResult> commit(
                final List<Mutation> mutations
            ) {
                return DataLockClient.this.commit(mutations, txn);
            }

            @Override
            public CompletableFuture<RunQueryResponse> runQuery(final Query query) {
                return DataLockClient.this.runQuery(query,
                    Optional.of(ReadOptions.fromTransaction(txn)));
            }

            @Override
            public CompletableFuture<LookupResponse> lookup(final List<Key> keys) {
                return DataLockClient.this.lookup(keys,
                    Optional.of(ReadOptions.fromTransaction(txn)));
            }
        });
    }

    public Key key(final PathElement... elements) {
        return key(Stream.of(elements).collect(Collectors.toList()));
    }

    public Key key(final List<PathElement> elements) {
        final List<PathElement> path = new ArrayList<>(elements);

        if (!path.stream().limit(path.size() - 1).allMatch(PathElement::isComplete)) {
            throw new IllegalArgumentException(
                "Only the last element in path may be incomplete: " + path);
        }

        return new Key(path, partitionId);
    }

    public interface ThrowingFunction<A, B> {
        B apply(A input) throws Exception;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        public static final String DEFAULT_URL = "https://datastore.googleapis.com";
        public static final String DEFAULT_PROJECT_ID = "datastore";

        private Optional<Credential> credential = Optional.empty();
        private Optional<String> url = Optional.empty();
        private Optional<String> projectId = Optional.empty();
        private Optional<String> namespace = Optional.empty();

        private Optional<ScheduledExecutorService> scheduler = Optional.empty();

        public Builder credential(final Credential credential) {
            this.credential = Optional.of(credential);
            return this;
        }

        public Builder url(final String url) {
            this.url = Optional.of(url);
            return this;
        }

        public Builder projectId(final String projectId) {
            this.projectId = Optional.of(projectId);
            return this;
        }

        public Builder namespace(final String namespace) {
            this.namespace = Optional.of(namespace);
            return this;
        }

        public Builder scheduler(final ScheduledExecutorService scheduler) {
            this.scheduler = Optional.of(scheduler);
            return this;
        }

        public DataLockClient build() {
            final OkHttpClient.Builder client = new OkHttpClient.Builder();

            final String url = this.url.orElse(DEFAULT_URL);
            final String projectId = this.projectId.orElse(DEFAULT_PROJECT_ID);

            final ScheduledExecutorService scheduler =
                this.scheduler.orElseGet(Executors::newSingleThreadScheduledExecutor);

            return new DataLockClient(client.build(), url, projectId, namespace, credential,
                scheduler);
        }
    }
}
