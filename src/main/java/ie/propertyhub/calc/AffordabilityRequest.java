package ie.propertyhub.calc;

import ie.propertyhub.domain.BuyerType;

/**
 * Inputs for an affordability check.
 *
 * @param grossIncome1  applicant 1 gross annual income (EUR)
 * @param grossIncome2  applicant 2 gross annual income (EUR), 0 if single applicant
 * @param savings       cash available for deposit and fees (EUR)
 * @param monthlyDebts  existing monthly loan repayments (car loan, etc.)
 * @param buyerType     buyer category under Central Bank rules
 * @param newBuild      buying a new build (Help to Buy eligibility)
 * @param ratePct       mortgage interest rate in percent
 * @param termYears     mortgage term in years
 */
public record AffordabilityRequest(
        long grossIncome1,
        long grossIncome2,
        long savings,
        long monthlyDebts,
        BuyerType buyerType,
        boolean newBuild,
        double ratePct,
        int termYears) {

    public AffordabilityRequest {
        if (buyerType == null) {
            buyerType = BuyerType.FIRST_TIME;
        }
        if (ratePct <= 0) {
            ratePct = 3.75;
        }
        if (termYears <= 0) {
            termYears = 30;
        }
    }

    public long householdIncome() {
        return Math.max(0, grossIncome1) + Math.max(0, grossIncome2);
    }
}
