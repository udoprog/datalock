package eu.toolchain.datalock;

import com.google.protobuf.ByteString;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class RunQueryResponse {
  private final List<KeyedEntity> entities;
  private final Optional<ByteString> cursor;
  private final ResultType resultType;

  public static RunQueryResponse fromPb(final com.google.datastore.v1.RunQueryResponse pb) {
    final com.google.datastore.v1.QueryResultBatch batch = pb.getBatch();

    final List<KeyedEntity> entities = new ArrayList<>();

    batch
        .getEntityResultsList()
        .stream()
        .filter(com.google.datastore.v1.EntityResult::hasEntity)
        .map(e -> KeyedEntity.fromPb(e.getEntity()))
        .forEach(entities::add);

    final Optional<ByteString> cursor = Optional.ofNullable(batch.getEndCursor());

    final ResultType resultType = ResultType.fromResultType(batch.getEntityResultType());
    return new RunQueryResponse(entities, cursor, resultType);
  }
}
