package eu.toolchain.datalock;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Data;

public interface Filter {
  String KEY_PROPERTY = "__key__";

  <T> T visit(Visitor<T> visitor);

  static PropertyFilter ancestorFilter(Key ancestor) {
    return keyFilter(PropertyFilter.Operator.HAS_ANCESTOR, ancestor);
  }

  static Composite compositeFilter(Filter... filters) {
    return new Composite(Stream.of(filters).collect(Collectors.toList()));
  }

  static Composite compositeFilter(List<Filter> filters) {
    return new Composite(new ArrayList<>(filters));
  }

  static PropertyFilter keyFilter(final PropertyFilter.Operator operator, final Key key) {
    return new PropertyFilter(KEY_PROPERTY, operator, new Value.KeyValue(key, false));
  }

  static PropertyFilter propertyFilter(
      final String property, final PropertyFilter.Operator operator, final Value value
  ) {
    return new PropertyFilter(property, operator, value);
  }

  interface Visitor<T> {
    T visitComposite(Composite composite);

    T visitPropertyFilter(PropertyFilter propertyFilter);
  }

  @Data
  class Composite implements Filter {
    private final List<Filter> filters;

    @Override
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitComposite(this);
    }
  }

  @Data
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
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitPropertyFilter(this);
    }

    @Override
    public String toString() {
      return "Property{" + property + " " + operator + " " + value + "}";
    }

    public static enum Operator {
      LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL, EQUAL, HAS_ANCESTOR;

      @Override
      public String toString() {
        switch (this) {
          case LESS_THAN:
            return "<";
          case LESS_THAN_OR_EQUAL:
            return "<=";
          case GREATER_THAN:
            return ">";
          case GREATER_THAN_OR_EQUAL:
            return ">=";
          case EQUAL:
            return "==";
          case HAS_ANCESTOR:
            return "^";
          default:
            return "?";
        }
      }
    }
  }
}
