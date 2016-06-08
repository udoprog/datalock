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
    public com.google.datastore.v1beta3.Key.PathElement toPb() {
        return com.google.datastore.v1beta3.Key.PathElement.newBuilder().setKind(kind).build();
    }

    @Override
    public String toString() {
        return "<" + kind + ": incomplete>";
    }
}
