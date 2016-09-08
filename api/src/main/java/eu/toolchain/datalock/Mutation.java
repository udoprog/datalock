package eu.toolchain.datalock;

import lombok.Data;

public interface Mutation {
  <T> T visit(Visitor<? extends T> visitor);

  static Mutation delete(final Key key) {
    return new DeleteMutation(key);
  }

  static Mutation insert(final Entity.KeyedEntity entity) {
    return new InsertMutation(entity);
  }

  static Mutation upsert(final Entity.KeyedEntity entity) {
    return new UpsertMutation(entity);
  }

  static Mutation update(Entity.KeyedEntity entity) {
    return new UpdateMutation(entity);
  }

  interface Visitor<T> {
    T visitDelete(DeleteMutation delete);

    T visitInsert(InsertMutation insert);

    T visitUpsert(UpsertMutation upsert);

    T visitUpdate(UpdateMutation update);
  }

  @Data
  class DeleteMutation implements Mutation {
    private final Key key;

    @Override
    public <T> T visit(final Visitor<? extends T> visitor) {
      return visitor.visitDelete(this);
    }
  }

  @Data
  class InsertMutation implements Mutation {
    private final Entity.KeyedEntity entity;

    @Override
    public <T> T visit(final Visitor<? extends T> visitor) {
      return visitor.visitInsert(this);
    }
  }

  @Data
  class UpsertMutation implements Mutation {
    private final Entity.KeyedEntity entity;

    @Override
    public <T> T visit(final Visitor<? extends T> visitor) {
      return visitor.visitUpsert(this);
    }
  }

  @Data
  class UpdateMutation implements Mutation {
    private final Entity.KeyedEntity entity;

    @Override
    public <T> T visit(final Visitor<? extends T> visitor) {
      return visitor.visitUpdate(this);
    }
  }
}
