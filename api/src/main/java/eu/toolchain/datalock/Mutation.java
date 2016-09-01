package eu.toolchain.datalock;

import lombok.Data;

public interface Mutation {
  com.google.datastore.v1.Mutation toPb();

  static Mutation delete(final Key key) {
    return new DeleteMutation(key);
  }

  static Mutation insert(final KeyedEntity entity) {
    return new InsertMutation(entity);
  }

  static Mutation upsert(final KeyedEntity entity) {
    return new UpsertMutation(entity);
  }

  static Mutation update(KeyedEntity entity) {
    return new UpdateMutation(entity);
  }

  @Data
  class DeleteMutation implements Mutation {
    private final Key key;

    @Override
    public com.google.datastore.v1.Mutation toPb() {
      final com.google.datastore.v1.Mutation.Builder builder =
          com.google.datastore.v1.Mutation.newBuilder();
      builder.setDelete(key.toPb());
      return builder.build();
    }
  }

  @Data
  class InsertMutation implements Mutation {
    private final KeyedEntity entity;

    @Override
    public com.google.datastore.v1.Mutation toPb() {
      final com.google.datastore.v1.Mutation.Builder builder =
          com.google.datastore.v1.Mutation.newBuilder();
      builder.setInsert(entity.toPb());
      return builder.build();
    }
  }

  @Data
  class UpsertMutation implements Mutation {
    private final KeyedEntity entity;

    @Override
    public com.google.datastore.v1.Mutation toPb() {
      final com.google.datastore.v1.Mutation.Builder builder =
          com.google.datastore.v1.Mutation.newBuilder();
      builder.setUpsert(entity.toPb());
      return builder.build();
    }
  }

  @Data
  class UpdateMutation implements Mutation {
    private final KeyedEntity entity;

    @Override
    public com.google.datastore.v1.Mutation toPb() {
      final com.google.datastore.v1.Mutation.Builder builder =
          com.google.datastore.v1.Mutation.newBuilder();
      builder.setUpdate(entity.toPb());
      return builder.build();
    }
  }
}
