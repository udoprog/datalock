package eu.toolchain.datalock;

public interface PathElement {
    String kind();

    boolean isComplete();

    com.google.datastore.v1beta3.Key.PathElement toPb();

    static PathElement element(final String kind) {
        return new IncompletePathElement(kind);
    }

    static PathElement element(final String kind, final String name) {
        return new NamePathElement(kind, name);
    }

    static PathElement element(final String kind, final long id) {
        return new IdPathElement(kind, id);
    }

    static PathElement fromPb(final com.google.datastore.v1beta3.Key.PathElement pb) {
        final String kind = pb.getKind();

        switch (pb.getIdTypeCase()) {
            case ID:
                return new IdPathElement(kind, pb.getId());
            case NAME:
                return new NamePathElement(kind, pb.getName());
            default:
                return new IncompletePathElement(kind);
        }
    }
}
