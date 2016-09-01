package eu.toolchain.datalock;

import com.google.protobuf.ByteString;
import lombok.Data;

@Data
public class Transaction {
  private final ByteString bytes;
}
