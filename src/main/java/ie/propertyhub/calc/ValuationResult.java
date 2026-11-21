package ie.propertyhub.calc;

/**
 * Automated valuation (like Zillow's Zestimate) for a property.
 *
 * @param estimate        central estimate in euro
 * @param low             lower bound of the range
 * @param high            upper bound of the range
 * @param monthlyRent     estimated achievable monthly rent
 * @param pricePerSqm     estimate per square metre
 * @param oneYearForecast estimate in 12 months at the county's trend growth rate
 */
public record ValuationResult(long estimate, long low, long high, long monthlyRent, long pricePerSqm,
                              long oneYearForecast) {
}
