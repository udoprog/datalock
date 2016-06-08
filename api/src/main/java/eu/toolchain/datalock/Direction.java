package eu.toolchain.datalock;

public enum Direction {
    ASCENDING(com.google.datastore.v1beta3.PropertyOrder.Direction.ASCENDING),
    DESCENDING(com.google.datastore.v1beta3.PropertyOrder.Direction.DESCENDING);

    private final com.google.datastore.v1beta3.PropertyOrder.Direction direction;

    Direction(com.google.datastore.v1beta3.PropertyOrder.Direction direction) {
        this.direction = direction;
    }

    public com.google.datastore.v1beta3.PropertyOrder.Direction direction() {
        return direction;
    }
}
