package eu.toolchain.datalock;

import lombok.Data;

@Data
public class PropertyOrder {
  private final String field;
  private final Direction direction;

  public static PropertyOrder propertyOrder(final String field) {
    return propertyOrder(field, Direction.ASCENDING);
  }

  public static PropertyOrder propertyOrder(final String field, final Direction direction) {
    return new PropertyOrder(field, direction);
  }

  public enum Direction {
    ASCENDING, DESCENDING;
  }
}
