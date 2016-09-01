package eu.toolchain.datalock;

import eu.toolchain.datalock.encoding.BooleanDecoder;
import eu.toolchain.datalock.encoding.BooleanEncoder;
import eu.toolchain.datalock.encoding.DoubleEncoder;
import eu.toolchain.datalock.encoding.ExcludeFromIndexesDecoder;
import eu.toolchain.datalock.encoding.ExcludeFromIndexesEncoder;
import eu.toolchain.datalock.encoding.FloatEncoder;
import eu.toolchain.datalock.encoding.IntegerEncoder;
import eu.toolchain.datalock.encoding.KeyDecoder;
import eu.toolchain.datalock.encoding.KeyEncoder;
import eu.toolchain.datalock.encoding.ListDecoder;
import eu.toolchain.datalock.encoding.ListEncoder;
import eu.toolchain.datalock.encoding.LongEncoder;
import eu.toolchain.datalock.encoding.MapDecoder;
import eu.toolchain.datalock.encoding.MapEncoder;
import eu.toolchain.datalock.encoding.NumberDecoder;
import eu.toolchain.datalock.encoding.ShortEncoder;
import eu.toolchain.datalock.encoding.StringDecoder;
import eu.toolchain.datalock.encoding.StringEncoder;
import eu.toolchain.datalock.encoding.ValueDecoder;
import eu.toolchain.datalock.encoding.ValueEncoder;
import eu.toolchain.scribe.Decoded;
import eu.toolchain.scribe.Decoder;
import eu.toolchain.scribe.DecoderFactory;
import eu.toolchain.scribe.DecoderRegistry;
import eu.toolchain.scribe.Encoder;
import eu.toolchain.scribe.EncoderFactory;
import eu.toolchain.scribe.EncoderRegistry;
import eu.toolchain.scribe.EntityFieldsDecoder;
import eu.toolchain.scribe.EntityFieldsEncoder;
import eu.toolchain.scribe.EntityResolver;
import eu.toolchain.scribe.Flags;
import eu.toolchain.scribe.reflection.JavaType;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static eu.toolchain.scribe.TypeMatcher.any;
import static eu.toolchain.scribe.TypeMatcher.instance;
import static eu.toolchain.scribe.TypeMatcher.isPrimitive;
import static eu.toolchain.scribe.TypeMatcher.type;

@RequiredArgsConstructor
public class DataLockEncodingFactory implements EncoderFactory<Value>, DecoderFactory<Value> {
  private static EncoderRegistry<Value> encoders = new EncoderRegistry<>();
  private static DecoderRegistry<Value> decoders = new DecoderRegistry<>();

  private final EntityResolver resolver;

  static {
    encoders.encoder(type(Map.class, any(), any()), (resolver, type, factory) -> {
      final JavaType first = type.getTypeParameter(0).get();
      final JavaType second = type.getTypeParameter(1).get();

      if (!JavaType.of(String.class).equals(first)) {
        throw new IllegalArgumentException("First type argument must be String (" + type + ")");
      }

      final Encoder<Value, Object> value =
          resolver.mapping(second).newEncoderImmediate(resolver, Flags.empty(), factory);
      return new MapEncoder<>(value);
    });

    encoders.encoder(type(List.class, any()), (resolver, type, factory) -> {
      final JavaType first = type.getTypeParameter(0).get();

      final Encoder<Value, Object> value =
          resolver.mapping(first).newEncoderImmediate(resolver, Flags.empty(), factory);

      return new ListEncoder<>(value);
    });

    encoders.encoder(type(String.class), (resolver, type, factory) -> StringEncoder.get());

    encoders.encoder(isPrimitive(Boolean.class), (resolver, type, factory) -> BooleanEncoder.get());

    encoders.encoder(isPrimitive(Short.class), (resolver, type, factory) -> ShortEncoder.get());
    encoders.encoder(isPrimitive(Integer.class), (resolver, type, factory) -> IntegerEncoder.get());
    encoders.encoder(isPrimitive(Long.class), (resolver, type, factory) -> LongEncoder.get());

    encoders.encoder(isPrimitive(Float.class), (resolver, type, factory) -> FloatEncoder.get());

    encoders.encoder(isPrimitive(Double.class), (resolver, type, factory) -> DoubleEncoder.get());

    encoders.encoder(instance(Value.class), (resolver, type, factory) -> ValueEncoder.get());
    encoders.encoder(instance(Key.class), (resolver, type, factory) -> KeyEncoder.get());
  }

  static {
    decoders.decoder(type(Map.class, any(), any()), (resolver, type, factory) -> {
      final JavaType first = type.getTypeParameter(0).get();
      final JavaType second = type.getTypeParameter(1).get();

      if (!JavaType.of(String.class).equals(first)) {
        throw new IllegalArgumentException("First type argument must be String (" + type + ")");
      }

      final Decoder<Value, Object> value =
          resolver.mapping(second).newDecoderImmediate(resolver, Flags.empty(), factory);
      return new MapDecoder<>(value);
    });

    decoders.decoder(type(List.class, any()), (resolver, type, factory) -> {
      final JavaType first = type.getTypeParameter(0).get();

      final Decoder<Value, Object> value =
          resolver.mapping(first).newDecoderImmediate(resolver, Flags.empty(), factory);

      return new ListDecoder<>(value);
    });

    decoders.decoder(type(String.class), (resolver, type, factory) -> StringDecoder.get());

    decoders.decoder(isPrimitive(Boolean.class), (resolver, type, factory) -> BooleanDecoder.get());

    decoders.decoder(isPrimitive(Short.class), (resolver, type, factory) -> NumberDecoder.SHORT);
    decoders.decoder(isPrimitive(Integer.class),
        (resolver, type, factory) -> NumberDecoder.INTEGER);
    decoders.decoder(isPrimitive(Long.class), (resolver, type, factory) -> NumberDecoder.LONG);
    decoders.decoder(isPrimitive(Float.class), (resolver, type, factory) -> NumberDecoder.FLOAT);
    decoders.decoder(isPrimitive(Double.class), (resolver, type, factory) -> NumberDecoder.DOUBLE);

    decoders.decoder(instance(Value.class), (resolver, type, factory) -> ValueDecoder.get());

    decoders.decoder(instance(Key.class), (resolver, type, factory) -> KeyDecoder.get());
  }

  @Override
  public <Source> Stream<Encoder<Value, Source>> newEncoder(
      final EntityResolver resolver, final Flags flags, final JavaType type
  ) {
    Stream<Encoder<Value, Source>> encoder = encoders.newEncoder(resolver, type, this);

    if (flags.getFlag(DataLockFlags.ExcludeFromIndexes.class).findFirst().isPresent()) {
      encoder = encoder.map(ExcludeFromIndexesEncoder::new);
    }

    return encoder;
  }

  @Override
  public <Source> Stream<Decoder<Value, Source>> newDecoder(
      final EntityResolver resolver, final Flags flags, final JavaType type
  ) {
    Stream<Decoder<Value, Source>> decoder = decoders.newDecoder(resolver, type, this);

    final boolean strictCheck = flags
        .getFlag(DataLockFlags.ExcludeFromIndexes.class)
        .map(DataLockFlags.ExcludeFromIndexes::isDecode)
        .findFirst()
        .orElse(false);

    if (strictCheck) {
      decoder = decoder.map(ExcludeFromIndexesDecoder::new);
    }

    return decoder;
  }

  @Override
  public EntityFieldsEncoder<Value> newEntityEncoder() {
    return new DataLockEntityFieldsEncoder();
  }

  @Override
  public Decoded<EntityFieldsDecoder<Value>> newEntityDecoder(final Value instance) {
    return instance.visit(new Value.Visitor<Decoded<EntityFieldsDecoder<Value>>>() {
      @Override
      public Decoded<EntityFieldsDecoder<Value>> visitEntity(
          final Value.EntityValue entity
      ) {
        final Entity e = entity.getEntity();
        final Optional<Key> key = e.asKeyed().map(KeyedEntity::getKey);

        return Decoded.of(new DataLockEntityFieldsDecoder(
            () -> key.map(Decoded::of).orElseGet(Decoded::absent).map(Value::fromKey),
            e.getProperties()));
      }

      @Override
      public Decoded<EntityFieldsDecoder<Value>> visitNull(final Value.NullValue nothing) {
        return Decoded.absent();
      }
    });
  }
}
