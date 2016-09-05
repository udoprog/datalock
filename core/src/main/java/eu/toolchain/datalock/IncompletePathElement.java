package eu.toolchain.datalock;

import lombok.Data;

@Data
public class IncompletePathElement implements PathElement {
  private final String kind;

  @Override
  public String kind() {
    return kind;
  }

  @Override
  public boolean isComplete() {
    return false;
  }

  @Override
  public <T> T visit(final Visitor<? extends T> visitor) {
    return visitor.visitIncompletePathElement(this);
  }

  @Override
  public String toString() {
    return "<" + kind + ": incomplete>";
  }
}
