package eu.toolchain.datalock;

import com.google.datastore.v1beta3.EntityResult;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class LookupResponse {
    private final List<Key> deferred;
    private final List<Entity> found;
    private final List<Entity> missing;

    public static LookupResponse fromPb(final com.google.datastore.v1beta3.LookupResponse pb) {
        final List<Key> deferred =
            pb.getDeferredList().stream().map(Key::fromPb).collect(Collectors.toList());

        final List<Entity> found = pb
            .getFoundList()
            .stream()
            .filter(EntityResult::hasEntity)
            .map(EntityResult::getEntity)
            .map(Entity::fromPb)
            .collect(Collectors.toList());

        final List<Entity> missing = pb
            .getMissingList()
            .stream()
            .filter(EntityResult::hasEntity)
            .map(EntityResult::getEntity)
            .map(Entity::fromPb)
            .collect(Collectors.toList());

        return new LookupResponse(deferred, found, missing);
    }
}
