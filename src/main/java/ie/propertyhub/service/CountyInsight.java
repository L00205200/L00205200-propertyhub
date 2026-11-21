package ie.propertyhub.service;

import ie.propertyhub.domain.County;

/**
 * Market snapshot for one county.
 *
 * @param county            the county
 * @param forSale           active sale listings
 * @param toRent            active rental listings
 * @param avgAskingPrice    average asking price of sale listings (0 if none)
 * @param avgPricePerSqm    reference EUR/m2 used by the valuation model
 * @param avgRentTwoBed     reference 2-bed monthly rent
 * @param grossYieldPct     average gross yield of sale listings, using estimated rent
 * @param annualGrowthPct   reference annual price growth
 * @param incomeNeededFtb   income a first-time buyer needs for the county's average asking price
 */
public record CountyInsight(
        County county,
        long forSale,
        long toRent,
        long avgAskingPrice,
        int avgPricePerSqm,
        int avgRentTwoBed,
        double grossYieldPct,
        double annualGrowthPct,
        long incomeNeededFtb) {
}
