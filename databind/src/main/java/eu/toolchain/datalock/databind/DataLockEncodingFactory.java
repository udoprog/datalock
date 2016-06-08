package eu.toolchain.datalock.databind;

import eu.toolchain.datalock.Value;
import eu.toolchain.ogt.EncodingFactory;

public class DataLockEncodingFactory implements EncodingFactory<Value> {
    @Override
    public DataLockTypeEncoder fieldEncoder() {
        return new DataLockTypeEncoder();
    }

    @Override
    public DataLockTypeDecoder fieldDecoder() {
        return new DataLockTypeDecoder();
    }
}
