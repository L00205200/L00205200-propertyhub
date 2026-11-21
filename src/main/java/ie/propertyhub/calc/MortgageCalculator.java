package ie.propertyhub.calc;

/** Standard annuity (capital and interest) mortgage repayment maths. */
public final class MortgageCalculator {

    /**
     * Monthly repayment for a capital-and-interest mortgage.
     *
     * @param principal     amount borrowed in euro
     * @param annualRatePct annual interest rate in percent, e.g. 3.75
     * @param years         term in years
     */
    public double monthlyRepayment(double principal, double annualRatePct, int years) {
        if (principal <= 0 || years <= 0) {
            return 0;
        }
        int months = years * 12;
        double r = annualRatePct / 100.0 / 12.0;
        if (r == 0) {
            return principal / months;
        }
        return principal * r / (1 - Math.pow(1 + r, -months));
    }

    /** Total interest paid over the full term. */
    public double totalInterest(double principal, double annualRatePct, int years) {
        return monthlyRepayment(principal, annualRatePct, years) * years * 12 - Math.max(principal, 0);
    }
}
