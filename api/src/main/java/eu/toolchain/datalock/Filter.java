package eu.toolchain.datalock;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Filter {
  String KEY_PROPERTY = "__key__";

  com.google.datastore.v1.Filter toPb();

  static PropertyFilter ancestorFilter(Key ancestor) {
    return keyFilter(Operator.HAS_ANCESTOR, ancestor);
  }

  static Composite compositeFilter(Filter... filters) {
    return new Composite(Stream.of(filters).collect(Collectors.toList()));
  }

  static Composite compositeFilter(List<Filter> filters) {
    return new Composite(new ArrayList<>(filters));
  }

  static PropertyFilter keyFilter(final Operator operator, final Key key) {
    return new PropertyFilter(KEY_PROPERTY, operator, new Value.KeyValue(key, false));
  }

  static PropertyFilter propertyFilter(
      final String property, final Operator operator, final Value value
  ) {
    return new PropertyFilter(property, operator, value);
  }

  @Data
  class Composite implements Filter {
    private final List<Filter> filters;

    @Override
    public com.google.datastore.v1.Filter toPb() {
      final com.google.datastore.v1.CompositeFilter.Builder builder =
          com.google.datastore.v1.CompositeFilter.newBuilder();
      filters.stream().map(Filter::toPb).forEach(builder::addFilters);
      builder.setOp(com.google.datastore.v1.CompositeFilter.Operator.AND);
      return com.google.datastore.v1.Filter
          .newBuilder()
          .setCompositeFilter(builder.build())
          .build();
    }
  }

  class PropertyFilter implements Filter {
    private final String property;
    private final Operator operator;
    private final Value value;

    public PropertyFilter(final String property, final Operator operator, final Value value) {
      this.property = property;
      this.operator = operator;
      this.value = value;
    }

    @Override
    public com.google.datastore.v1.Filter toPb() {
      final com.google.datastore.v1.PropertyFilter.Builder builder =
          com.google.datastore.v1.PropertyFilter.newBuilder();
      builder.setProperty(
          com.google.datastore.v1.PropertyReference.newBuilder().setName(property).build());
      builder.setOp(operator.operator());
      builder.setValue(value.toPb());
      return com.google.datastore.v1.Filter.newBuilder().setPropertyFilter(builder.build()).build();
    }

    @Override
    public String toString() {
      return "Property{" + property + " " + operator + " " + value + "}";
    }
  }
}
