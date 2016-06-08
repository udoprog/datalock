package eu.toolchain.datalock;

public enum ReadConsistency {
    READ_CONSISTENCY_UNSPECIFIED(
        com.google.datastore.v1beta3.ReadOptions.ReadConsistency.READ_CONSISTENCY_UNSPECIFIED),
    STRONG(com.google.datastore.v1beta3.ReadOptions.ReadConsistency.STRONG),
    EVENTUAL(com.google.datastore.v1beta3.ReadOptions.ReadConsistency.EVENTUAL);

    private final com.google.datastore.v1beta3.ReadOptions.ReadConsistency readConsistency;

    ReadConsistency(
        final com.google.datastore.v1beta3.ReadOptions.ReadConsistency readConsistency
    ) {
        this.readConsistency = readConsistency;
    }

    public com.google.datastore.v1beta3.ReadOptions.ReadConsistency readConsistency() {
        return readConsistency;
    }
}
