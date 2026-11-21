package ie.propertyhub.calc;

import ie.propertyhub.domain.BuyerType;

/**
 * Rental yield and buy-to-let cashflow. Cost assumptions are illustrative defaults:
 * 4% vacancy, 8% letting/management fee, 5% maintenance, EUR 450 insurance,
 * local property tax approx. 0.1% of value, EUR 40 RTB registration.
 */
public final class RentalYieldCalculator {

    static final double VACANCY = 0.04;
    static final double MANAGEMENT = 0.08;
    static final double MAINTENANCE = 0.05;
    static final long INSURANCE = 450;
    static final double LPT_RATE = 0.001;
    static final long RTB_FEE = 40;

    private final MortgageCalculator mortgageCalc;
    private final StampDutyCalculator stampDuty;

    public RentalYieldCalculator(MortgageCalculator mortgageCalc, StampDutyCalculator stampDuty) {
        this.mortgageCalc = mortgageCalc;
        this.stampDuty = stampDuty;
    }

    public YieldResult analyse(long price, long monthlyRent, double ratePct, int termYears) {
        if (price <= 0 || monthlyRent <= 0) {
            return new YieldResult(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "n/a");
        }
        long annualRent = monthlyRent * 12;
        double costs = annualRent * (VACANCY + MANAGEMENT + MAINTENANCE) + INSURANCE + price * LPT_RATE + RTB_FEE;
        long annualCosts = Math.round(costs);
        long net = annualRent - annualCosts;
        double gross = annualRent * 100.0 / price;
        double netYield = net * 100.0 / price;

        long loan = Math.round(price * BuyerType.BUY_TO_LET.getLoanToValue());
        long repayment = Math.round(mortgageCalc.monthlyRepayment(loan, ratePct, termYears));
        long cashflow = Math.round(net / 12.0) - repayment;
        long cashInvested = price - loan + stampDuty.residential(price) + AffordabilityCalculator.STANDARD_FEES;
        double coc = cashInvested <= 0 ? 0 : cashflow * 12 * 100.0 / cashInvested;

        return new YieldResult(annualRent, annualCosts, net, round1(gross), round1(netYield), loan, repayment,
                cashflow, cashInvested, round1(coc), rating(gross));
    }

    static String rating(double grossYieldPct) {
        if (grossYieldPct >= 7) {
            return "Strong";
        }
        if (grossYieldPct >= 5.5) {
            return "Good";
        }
        if (grossYieldPct >= 4) {
            return "Moderate";
        }
        return "Low";
    }

    static double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
