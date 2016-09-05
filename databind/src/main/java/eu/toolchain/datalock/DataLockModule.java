package eu.toolchain.datalock;

import eu.toolchain.scribe.EncodedMapping;
import eu.toolchain.scribe.EntityMapperBuilder;
import eu.toolchain.scribe.Module;

import java.util.stream.Stream;

import static eu.toolchain.scribe.TypeMatcher.type;
import static eu.toolchain.scribe.detector.MappingDetector.matchMapping;

public class DataLockModule implements Module {
  @Override
  public void register(final EntityMapperBuilder builder) {
    builder.mapping(matchMapping(type(Value.class), EncodedMapping::new));
    builder.mapping(matchMapping(type(Key.class), EncodedMapping::new));

    builder.flag((resolver, java, annotations) -> {
      if (annotations.isAnnotationPresent(EntityKey.class)) {
        return Stream.of(DataLockFlags.KEY_FIELD);
      }

      return Stream.of();
    });

    builder.flag((resolver, java, annotations) -> {
      return annotations.getAnnotation(ExcludeFromIndexes.class).map(a -> {
        return new DataLockFlags.ExcludeFromIndexes(a.decode());
      });
    });
  }
}
