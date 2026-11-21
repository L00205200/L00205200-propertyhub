package ie.propertyhub.calc;

import ie.propertyhub.domain.BuyerType;

/**
 * Help to Buy (HTB): first-time buyers of a new build (or self-build) can claim the lesser of
 * EUR 30,000 or 10% of the purchase price, where the price is EUR 500,000 or less.
 * Check Revenue for the current scheme end date and full eligibility conditions.
 */
public final class HelpToBuyCalculator {

    public static final long MAX_RELIEF = 30_000L;
    public static final long PRICE_CAP = 500_000L;

    public long relief(long price, BuyerType buyerType, boolean newBuild) {
        if (buyerType != BuyerType.FIRST_TIME || !newBuild || price <= 0 || price > PRICE_CAP) {
            return 0;
        }
        return Math.min(MAX_RELIEF, Math.round(price * 0.10));
    }
}
