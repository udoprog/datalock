package eu.toolchain.datalock;

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

        public Query build() {
            return new Query(filter, distinctOn, order, kind);
        }
    }
}
