package ie.propertyhub.domain;

/** Property types with relative value and rent factors used by the valuation engine. */
public enum PropertyType {
    DETACHED("Detached house", 1.05, 1.10),
    SEMI_DETACHED("Semi-detached house", 1.00, 1.05),
    TERRACED("Terraced house", 0.97, 1.00),
    BUNGALOW("Bungalow", 0.95, 1.00),
    APARTMENT("Apartment", 1.08, 1.00);

    private final String displayName;
    private final double valueFactor;
    private final double rentFactor;

    PropertyType(String displayName, double valueFactor, double rentFactor) {
        this.displayName = displayName;
        this.valueFactor = valueFactor;
        this.rentFactor = rentFactor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getValueFactor() {
        return valueFactor;
    }

    public double getRentFactor() {
        return rentFactor;
    }
}
