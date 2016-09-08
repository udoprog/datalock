package eu.toolchain.datalock;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import lombok.Data;

@Data
public class RunQueryResponse {
  private final List<Entity.KeyedEntity> entities;
  private final Optional<ByteBuffer> cursor;
  private final ResultType resultType;

  public enum ResultType {
    FULL, PROJECTION, KEY_ONLY;
  }
}
