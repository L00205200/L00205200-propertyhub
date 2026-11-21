package ie.propertyhub.calc;

/**
 * Irish residential stamp duty (instruments executed on or after 2 October 2024):
 * 1% up to EUR 1m, 2% on the portion from EUR 1m to EUR 1.5m, 6% above EUR 1.5m.
 */
public final class StampDutyCalculator {

    static final long BAND_1 = 1_000_000L;
    static final long BAND_2 = 1_500_000L;

    public long residential(long price) {
        if (price <= 0) {
            return 0;
        }
        double duty = Math.min(price, BAND_1) * 0.01;
        if (price > BAND_1) {
            duty += (Math.min(price, BAND_2) - BAND_1) * 0.02;
        }
        if (price > BAND_2) {
            duty += (price - BAND_2) * 0.06;
        }
        return Math.round(duty);
    }
}
