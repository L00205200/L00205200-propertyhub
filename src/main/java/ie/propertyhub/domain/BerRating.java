package ie.propertyhub.domain;

/**
 * Building Energy Rating (BER) as used on Irish property listings.
 * Better-rated homes typically command a price premium ("green premium").
 */
public enum BerRating {
    A1, A2, A3, B1, B2, B3, C1, C2, C3, D1, D2, E1, E2, F, G;

    /** The letter band (A to G). */
    public String grade() {
        return name().substring(0, 1);
    }

    /** Lower is better: A=0 ... G=6. */
    public int bandIndex() {
        return "ABCDEFG".indexOf(grade());
    }

    /** Relative value adjustment applied by the valuation engine. */
    public double valueFactor() {
        return switch (grade()) {
            case "A" -> 1.08;
            case "B" -> 1.04;
            case "C" -> 1.00;
            case "D" -> 0.96;
            case "E" -> 0.93;
            default -> 0.90;
        };
    }

    /** Energy-efficient homes rent slightly better. */
    public double rentFactor() {
        return bandIndex() <= 1 ? 1.03 : 1.00;
    }

    /** True when this rating is at least as good as {@code other}. */
    public boolean atLeast(BerRating other) {
        return this.ordinal() <= other.ordinal();
    }
}
