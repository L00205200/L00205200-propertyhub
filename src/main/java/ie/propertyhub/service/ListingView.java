package ie.propertyhub.service;

import ie.propertyhub.calc.ValuationResult;
import ie.propertyhub.calc.YieldResult;
import ie.propertyhub.domain.Listing;

/**
 * A listing enriched with analytics, ready for display or the API.
 *
 * @param listing             the underlying listing
 * @param valuation           automated valuation and rent estimate
 * @param rentalYield         investment analysis (sale listings only, otherwise null)
 * @param valueGapPct         % the asking price/rent is below (+) or above (-) our estimate
 * @param dealLabel           human-friendly label for valueGapPct
 * @param stampDuty           stamp duty at asking price (sale only)
 * @param incomeNeededFtb     household income a first-time buyer needs for a 90% mortgage
 * @param cashNeededFtb       deposit + stamp duty + fees - Help to Buy for a first-time buyer
 * @param monthlyRepaymentFtb repayment on a 90% mortgage at the default rate and term
 * @param daysOnMarket        days since listed
 */
public record ListingView(
        Listing listing,
        ValuationResult valuation,
        YieldResult rentalYield,
        double valueGapPct,
        String dealLabel,
        long stampDuty,
        long incomeNeededFtb,
        long cashNeededFtb,
        long monthlyRepaymentFtb,
        long daysOnMarket) {

    public String dealCss() {
        if (valueGapPct >= 5) {
            return "deal-great";
        }
        if (valueGapPct >= 0) {
            return "deal-fair";
        }
        return "deal-high";
    }
}
