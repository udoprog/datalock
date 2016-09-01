package eu.toolchain.datalock;

import eu.toolchain.scribe.Flag;
import lombok.Data;

public interface DataLockFlags {
  KeyFlag KEY_FIELD = new KeyFlag();

  class KeyFlag implements Flag {
  }

  @Data
  class ExcludeFromIndexes implements Flag {
    private final boolean decode;
  }
}
