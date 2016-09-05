package eu.toolchain.datalock;

public interface PathElement {
  String kind();

  boolean isComplete();

  <T> T visit(Visitor<? extends T> visitor);

  static PathElement element(final String kind) {
    return new IncompletePathElement(kind);
  }

  static PathElement element(final String kind, final String name) {
    return new NamePathElement(kind, name);
  }

  static PathElement element(final String kind, final long id) {
    return new IdPathElement(kind, id);
  }

  interface Visitor<T> {
    T visitIdPathElement(IdPathElement idPathElement);

    T visitNamePathElement(NamePathElement namePathElement);

    T visitIncompletePathElement(IncompletePathElement incompletePathElement);
  }
}
