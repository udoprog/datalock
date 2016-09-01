package eu.toolchain.datalock;

import java.util.Optional;

public class DatastoreException extends RuntimeException {
  private static final long serialVersionUID = 1602389249233538348L;

  private final Optional<Integer> code;

  public DatastoreException(final String message) {
    super(message);
    this.code = Optional.empty();
  }

  public DatastoreException(final String message, final Throwable cause) {
    super(message, cause);
    this.code = Optional.empty();
  }

  public DatastoreException(final int code, final String message, final Throwable cause) {
    super(code + ": " + message, cause);
    this.code = Optional.of(code);
  }

  public DatastoreException(final int code, final String message) {
    super(code + ": " + message);
    this.code = Optional.of(code);
  }

  public DatastoreException(final Throwable t) {
    super(t);
    this.code = Optional.empty();
  }

  public Optional<Integer> getCode() {
    return code;
  }
}
