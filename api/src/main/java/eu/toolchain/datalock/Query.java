package eu.toolchain.datalock;

import com.google.protobuf.ByteString;
import com.google.protobuf.Int32Value;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class Query {
    private final Optional<Filter> filter;
    private final List<String> distinctOn;
    private final List<PropertyOrder> order;
    private final Optional<String> kind;
    private final Optional<Integer> limit;
    private final Optional<ByteString> startCursor;
    private final Optional<ByteString> endCursor;

    public com.google.datastore.v1beta3.Query toPb() {
        final com.google.datastore.v1beta3.Query.Builder builder =
            com.google.datastore.v1beta3.Query.newBuilder();

        filter.ifPresent(f -> builder.setFilter(f.toPb()));

        distinctOn
            .stream()
            .map(
                g -> com.google.datastore.v1beta3.PropertyReference.newBuilder().setName(g).build())
            .forEach(builder::addDistinctOn);

        order.stream().map(PropertyOrder::toPb).forEach(builder::addOrder);

        kind.ifPresent(k -> builder.addKind(
            com.google.datastore.v1beta3.KindExpression.newBuilder().setName(k).build()));

        startCursor.ifPresent(builder::setStartCursor);
        endCursor.ifPresent(builder::setEndCursor);

        limit.map(i -> Int32Value.newBuilder().setValue(i).build()).ifPresent(builder::setLimit);

        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Optional<Filter> filter = Optional.empty();
        private List<String> distinctOn = new ArrayList<>();
        private List<PropertyOrder> order = new ArrayList<>();
        private Optional<String> kind = Optional.empty();
        private Optional<Integer> limit = Optional.empty();
        private Optional<ByteString> startCursor = Optional.empty();
        private Optional<ByteString> endCursor = Optional.empty();

        public Builder filter(final Filter filter) {
            this.filter = Optional.of(filter);
            return this;
        }

        public Builder distinctOn(final List<String> groupBy) {
            this.distinctOn = new ArrayList<>(groupBy);
            return this;
        }

        public Builder order(final List<PropertyOrder> order) {
            this.order = new ArrayList<>(order);
            return this;
        }

        public Builder kind(final String kind) {
            this.kind = Optional.of(kind);
            return this;
        }

        public Builder limit(final int limit) {
            this.limit = Optional.of(limit);
            return this;
        }

        public Builder startCursor(final ByteString startCursor) {
            this.startCursor = Optional.of(startCursor);
            return this;
        }

        public Builder endCursor(final ByteString endCursor) {
            this.endCursor = Optional.of(endCursor);
            return this;
        }

        public Query build() {
            return new Query(filter, distinctOn, order, kind, limit, startCursor, endCursor);
        }
    }
}
