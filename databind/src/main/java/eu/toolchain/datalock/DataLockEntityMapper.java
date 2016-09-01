package eu.toolchain.datalock;

import eu.toolchain.scribe.EntityResolver;
import eu.toolchain.scribe.Option;
import eu.toolchain.scribe.TypeDecoderProvider;
import eu.toolchain.scribe.TypeEncoderProvider;
import eu.toolchain.scribe.TypeReference;
import lombok.Data;

import java.lang.reflect.Type;

@Data
public class DataLockEntityMapper {
  private final EntityResolver resolver;

  private final TypeEncoderProvider<Value> valueEncoder;
  private final TypeDecoderProvider<Value> valueDecoder;

  public DataLockEntityMapper(final EntityResolver resolver) {
    this.resolver = resolver;

    final DataLockEncodingFactory f = new DataLockEncodingFactory(resolver);

    this.valueEncoder = resolver.encoderFor(f);
    this.valueDecoder = resolver.decoderFor(f);
  }

  public DataLockEncoding<Object> encodingForType(final Type type) {
    return new DataLockEncoding<>(valueEncoder.newEncoder(type), valueDecoder.newDecoder(type));
  }

  public <T> DataLockEncoding<T> encodingFor(final Class<T> type) {
    return new DataLockEncoding<>(valueEncoder.newEncoder(type), valueDecoder.newDecoder(type));
  }

  public <T> DataLockEncoding<T> encodingFor(final TypeReference<T> type) {
    return new DataLockEncoding<>(valueEncoder.newEncoder(type), valueDecoder.newDecoder(type));
  }

  public DataLockEntityMapper withOptions(final Option... options) {
    return new DataLockEntityMapper(resolver.withOptions(options));
  }
}
