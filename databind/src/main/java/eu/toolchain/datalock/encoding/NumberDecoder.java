package eu.toolchain.datalock.encoding;

import eu.toolchain.datalock.Value;
import eu.toolchain.scribe.Context;
import eu.toolchain.scribe.Decoded;
import eu.toolchain.scribe.Decoder;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public class NumberDecoder<T extends Number> implements Decoder<Value, T> {
  private final Function<Number, T> converter;

  @Override
  public Decoded<T> decode(final Context path, final Value instance) {
    return instance.visit(new Value.Visitor<Decoded<T>>() {
      @Override
      public Decoded<T> visitDouble(final Value.DoubleValue value) {
        return Decoded.of(converter.apply(value.getValue()));
      }

      @Override
      public Decoded<T> visitInteger(final Value.IntegerValue value) {
        return Decoded.of(converter.apply(value.getValue()));
      }

      @Override
      public Decoded<T> visitNull(final Value.NullValue nothing) {
        return Decoded.absent();
      }

      @Override
      public Decoded<T> defaultAction(final Value value) {
        throw path.error("expected numerical value");
      }
    });
  }

  public static final NumberDecoder<Short> SHORT = new NumberDecoder<>(Number::shortValue);
  public static final NumberDecoder<Integer> INTEGER = new NumberDecoder<>(Number::intValue);
  public static final NumberDecoder<Long> LONG = new NumberDecoder<>(Number::longValue);
  public static final NumberDecoder<Float> FLOAT = new NumberDecoder<>(Number::floatValue);
  public static final NumberDecoder<Double> DOUBLE = new NumberDecoder<>(Number::doubleValue);
}
