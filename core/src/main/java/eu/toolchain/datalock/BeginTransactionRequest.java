package eu.toolchain.datalock;

import lombok.Data;

@Data
public class BeginTransactionRequest {
  public com.google.datastore.v1.BeginTransactionRequest toPb(final String projectId) {
    final com.google.datastore.v1.BeginTransactionRequest.Builder builder =
        com.google.datastore.v1.BeginTransactionRequest.newBuilder();
    builder.setProjectId(projectId);
    return builder.build();
  }
}
