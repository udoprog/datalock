package eu.toolchain.datalock;

import lombok.Data;

public interface ReadOptions {
  <T> T visit(Visitor<T> visitor);

  static ReadOptions fromTransaction(final Transaction transaction) {
    return new TransactionReadOptions(transaction);
  }

  static ReadOptions fromConsistency(final ReadConsistency consistency) {
    return new ConsistencyReadOptions(consistency);
  }

  static ReadOptions defaultInstance() {
    return new ConsistencyReadOptions(ReadConsistency.EVENTUAL);
  }

  interface Visitor<T> {
    T visitTransaction(TransactionReadOptions transaction);

    T visitConsistency(ConsistencyReadOptions transaction);
  }

  @Data
  class TransactionReadOptions implements ReadOptions {
    private final Transaction transaction;

    @Override
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitTransaction(this);
    }
  }

  @Data
  class ConsistencyReadOptions implements ReadOptions {
    private final ReadConsistency consistency;

    @Override
    public <T> T visit(final Visitor<T> visitor) {
      return visitor.visitConsistency(this);
    }
  }

  enum ReadConsistency {
    STRONG, EVENTUAL;
  }
}
