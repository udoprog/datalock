package eu.toolchain.datalock;

import lombok.Data;

@Data
public class NamePathElement implements PathElement {
  private final String kind;
  private final String name;

  @Override
  public String kind() {
    return kind;
  }

  public String name() {
    return name;
  }

  @Override
  public boolean isComplete() {
    return true;
  }

  @Override
  public <T> T visit(final Visitor<? extends T> visitor) {
    return visitor.visitNamePathElement(this);
  }

  @Override
  public String toString() {
    return "<" + kind + ": name:" + name + ">";
  }
}
