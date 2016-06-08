package eu.toolchain.datalock.databind;

import eu.toolchain.datalock.Value;
import eu.toolchain.ogt.EntityResolver;
import eu.toolchain.ogt.JavaType;
import eu.toolchain.ogt.TypeEncodingProvider;

public class DataLockEntityMapper {
    private final TypeEncodingProvider<Value> parent;

    public DataLockEntityMapper(final EntityResolver resolver) {
        this.parent = resolver.providerFor(new DataLockEncodingFactory());
    }

    public DataLockTypeEncoding<Object> encodingFor(final JavaType type) {
        return new DataLockTypeEncoding<Object>(parent.encodingFor(type));
    }

    public <T> DataLockTypeEncoding<T> encodingFor(final Class<T> type) {
        return new DataLockTypeEncoding<T>(parent.encodingFor(type));
    }
}
