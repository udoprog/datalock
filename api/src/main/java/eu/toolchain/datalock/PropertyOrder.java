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

  public com.google.datastore.v1.PropertyOrder toPb() {
    final com.google.datastore.v1.PropertyOrder.Builder builder =
        com.google.datastore.v1.PropertyOrder.newBuilder();
    builder.setProperty(
        com.google.datastore.v1.PropertyReference.newBuilder().setName(field).build());
    builder.setDirection(direction.direction());
    return builder.build();
  }
}
