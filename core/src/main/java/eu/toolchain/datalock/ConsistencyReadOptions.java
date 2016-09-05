package eu.toolchain.datalock;

import lombok.Data;

@Data
public class ConsistencyReadOptions implements ReadOptions {
  private final ReadConsistency consistency;

  @Override
  public com.google.datastore.v1.ReadOptions toPb() {
    final com.google.datastore.v1.ReadOptions.Builder builder =
        com.google.datastore.v1.ReadOptions.newBuilder();
    builder.setReadConsistency(consistency.readConsistency());
    return builder.build();
  }
}
