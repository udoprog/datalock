package eu.toolchain.datalock;

import java.nio.ByteBuffer;

import lombok.Data;

@Data
public class Transaction {
  private final ByteBuffer bytes;
}
