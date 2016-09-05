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
  public <T> T visit(final Visitor<? extends T> visitor) {
    return visitor.visitIdPathElement(this);
  }

  @Override
  public String toString() {
    return "<" + kind + ": id:" + id + ">";
  }
}
