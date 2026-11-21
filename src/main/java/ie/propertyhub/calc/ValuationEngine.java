package ie.propertyhub.calc;

import ie.propertyhub.domain.BerRating;
import ie.propertyhub.domain.County;
import ie.propertyhub.domain.Listing;
import ie.propertyhub.domain.PropertyType;

/**
 * A transparent hedonic-style valuation model:
 * value = county EUR/m2 x floor area x property-type factor x BER factor x new-build premium.
 * Rent = county 2-bed rent x bedroom multiplier x type factor x BER factor.
 */
public final class ValuationEngine {

    static final double RANGE = 0.07;
    static final double NEW_BUILD_PREMIUM = 1.05;

    public ValuationResult value(County county, PropertyType type, int beds, int floorAreaSqm,
                                 BerRating ber, boolean newBuild) {
        double raw = (double) county.getAvgPricePerSqm() * Math.max(floorAreaSqm, 0)
                * type.getValueFactor() * ber.valueFactor() * (newBuild ? NEW_BUILD_PREMIUM : 1.0);
        long estimate = roundTo(raw, 1000);
        long low = roundTo(raw * (1 - RANGE), 1000);
        long high = roundTo(raw * (1 + RANGE), 1000);
        long rent = monthlyRent(county, type, beds, ber);
        long perSqm = floorAreaSqm <= 0 ? 0 : Math.round(raw / floorAreaSqm);
        long forecast = roundTo(raw * (1 + county.getAnnualGrowthPct() / 100.0), 1000);
        return new ValuationResult(estimate, low, high, rent, perSqm, forecast);
    }

    public ValuationResult value(Listing l) {
        return value(l.county(), l.propertyType(), l.beds(), l.floorAreaSqm(), l.ber(), l.newBuild());
    }

    public long monthlyRent(County county, PropertyType type, int beds, BerRating ber) {
        double raw = county.getAvgRentTwoBed() * bedMultiplier(beds) * type.getRentFactor() * ber.rentFactor();
        return roundTo(raw, 10);
    }

    /**
     * Deal score versus our estimate: positive means asking price is below estimate.
     * Returned as a percentage, e.g. 6.2 means "6.2% below estimate".
     */
    public double discountToEstimatePct(Listing l) {
        long est = value(l).estimate();
        return est == 0 ? 0 : (est - l.price()) * 100.0 / est;
    }

    static double bedMultiplier(int beds) {
        if (beds <= 1) {
            return 0.78;
        }
        return switch (beds) {
            case 2 -> 1.00;
            case 3 -> 1.18;
            case 4 -> 1.35;
            default -> 1.50;
        };
    }

    static long roundTo(double value, long unit) {
        return Math.round(value / unit) * unit;
    }
}
