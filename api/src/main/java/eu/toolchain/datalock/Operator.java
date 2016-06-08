package eu.toolchain.datalock;

public enum Operator {
    LESS_THAN(com.google.datastore.v1beta3.PropertyFilter.Operator.LESS_THAN),
    LESS_THAN_OR_EQUAL(com.google.datastore.v1beta3.PropertyFilter.Operator.LESS_THAN_OR_EQUAL),
    GREATER_THAN(com.google.datastore.v1beta3.PropertyFilter.Operator.GREATER_THAN),
    GREATER_THAN_OR_EQUAL(com.google.datastore.v1beta3.PropertyFilter.Operator.GREATER_THAN_OR_EQUAL),
    EQUAL(com.google.datastore.v1beta3.PropertyFilter.Operator.EQUAL),
    HAS_ANCESTOR(com.google.datastore.v1beta3.PropertyFilter.Operator.HAS_ANCESTOR);

    private final com.google.datastore.v1beta3.PropertyFilter.Operator operator;

    Operator(final com.google.datastore.v1beta3.PropertyFilter.Operator operator) {
        this.operator = operator;
    }

    public com.google.datastore.v1beta3.PropertyFilter.Operator operator() {
        return operator;
    }

    @Override
    public String toString() {
        switch (this) {
            case LESS_THAN:
                return "<";
            case LESS_THAN_OR_EQUAL:
                return "<=";
            case GREATER_THAN:
                return ">";
            case GREATER_THAN_OR_EQUAL:
                return ">=";
            case EQUAL:
                return "==";
            case HAS_ANCESTOR:
                return "^";
            default:
                return "?";
        }
    }
}
