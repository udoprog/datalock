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
    public com.google.datastore.v1beta3.Key.PathElement toPb() {
        return com.google.datastore.v1beta3.Key.PathElement.newBuilder().setKind(kind).setName(name).build();
    }

    @Override
    public String toString() {
        return "<" + kind + ": name:" + name + ">";
    }
}
