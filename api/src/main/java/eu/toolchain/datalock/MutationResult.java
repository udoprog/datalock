package eu.toolchain.datalock;

import lombok.Data;

import java.util.Optional;

@Data
public class MutationResult {
    private final Optional<Key> key;

    public static MutationResult fromPb(final com.google.datastore.v1beta3.MutationResult pb) {
        final Optional<Key> key =
            pb.hasKey() ? Optional.of(Key.fromPb(pb.getKey())) : Optional.empty();
        return new MutationResult(key);
    }

    public static MutationResult empty() {
        return new MutationResult(Optional.empty());
    }
}
