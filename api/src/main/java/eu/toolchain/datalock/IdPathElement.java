package eu.toolchain.datalock;

import lombok.Data;

@Data
public class IdPathElement implements PathElement {
  private final String kind;
  private final long id;

  @Override
  public String kind() {
    return kind;
  }

  public long id() {
    return id;
  }

  @Override
  public boolean isComplete() {
    return true;
  }

  @Override
  public com.google.datastore.v1.Key.PathElement toPb() {
    return com.google.datastore.v1.Key.PathElement.newBuilder().setKind(kind).setId(id).build();
  }

  @Override
  public String toString() {
    return "<" + kind + ": id:" + id + ">";
  }
}
