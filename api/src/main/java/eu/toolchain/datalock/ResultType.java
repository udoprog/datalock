package eu.toolchain.datalock;

public enum ResultType {
  FULL(com.google.datastore.v1.EntityResult.ResultType.FULL), PROJECTION(
      com.google.datastore.v1.EntityResult.ResultType.PROJECTION), KEY_ONLY(
      com.google.datastore.v1.EntityResult.ResultType.KEY_ONLY);

  private final com.google.datastore.v1.EntityResult.ResultType type;

  ResultType(final com.google.datastore.v1.EntityResult.ResultType type) {
    this.type = type;
  }

  public com.google.datastore.v1.EntityResult.ResultType type() {
    return type;
  }

  public static ResultType fromResultType(
      final com.google.datastore.v1.EntityResult.ResultType input
  ) {
    switch (input) {
      case FULL:
        return FULL;
      case PROJECTION:
        return PROJECTION;
      case KEY_ONLY:
        return KEY_ONLY;
      default:
        throw new IllegalArgumentException("Unsupportedf result type: " + input);
    }
  }
}
