package ie.propertyhub.calc;

/**
 * Outcome of an affordability check.
 *
 * @param maxPrice                  highest purchase price that satisfies LTI, LTV and cash constraints
 * @param mortgage                  mortgage amount at that price
 * @param maxLoanByIncome           loan-to-income ceiling (income x multiple)
 * @param deposit                   deposit paid (price minus mortgage)
 * @param stampDuty                 stamp duty at that price
 * @param fees                      estimated legal, survey and valuation fees
 * @param helpToBuy                 Help to Buy relief applied
 * @param monthlyRepayment          repayment at the chosen rate
 * @param stressedMonthlyRepayment  repayment at the chosen rate + 2 percentage points
 * @param repaymentToIncomePct      (repayment + debts) as a share of estimated net monthly income
 * @param limitingFactor            which rule capped the budget
 */
public record AffordabilityResult(
        long maxPrice,
        long mortgage,
        long maxLoanByIncome,
        long deposit,
        long stampDuty,
        long fees,
        long helpToBuy,
        long monthlyRepayment,
        long stressedMonthlyRepayment,
        double repaymentToIncomePct,
        String limitingFactor) {
}
