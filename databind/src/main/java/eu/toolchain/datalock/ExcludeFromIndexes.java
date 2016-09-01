package eu.toolchain.datalock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcludeFromIndexes {
  /**
   * Verify that any decoded value also has the proper exclude from indexes flag set.
   * <p>
   * This option is very strict and is disabled by default.
   */
  boolean decode() default false;
}
