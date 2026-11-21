package ie.propertyhub.calc;

/**
 * Rental investment analysis.
 *
 * @param annualRent       gross annual rent
 * @param annualCosts      vacancy, management, maintenance, insurance, LPT, RTB registration
 * @param netAnnualIncome  annual rent minus costs (before mortgage interest and income tax)
 * @param grossYieldPct    annual rent / price
 * @param netYieldPct      net income / price
 * @param mortgage         buy-to-let mortgage at 70% LTV
 * @param monthlyRepayment buy-to-let mortgage repayment
 * @param monthlyCashflow  net monthly income minus repayment
 * @param cashInvested     deposit + stamp duty + fees
 * @param cashOnCashPct    annual cashflow / cash invested
 * @param rating           Strong / Good / Moderate / Low
 */
public record YieldResult(
        long annualRent,
        long annualCosts,
        long netAnnualIncome,
        double grossYieldPct,
        double netYieldPct,
        long mortgage,
        long monthlyRepayment,
        long monthlyCashflow,
        long cashInvested,
        double cashOnCashPct,
        String rating) {
}
