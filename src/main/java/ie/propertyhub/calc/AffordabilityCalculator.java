package ie.propertyhub.calc;

import ie.propertyhub.domain.BuyerType;

/**
 * Works out the maximum purchase price under Central Bank of Ireland mortgage measures
 * (loan-to-income and loan-to-value limits), the buyer's cash, stamp duty, fees and Help to Buy.
 */
public final class AffordabilityCalculator {

    /** Typical solicitor, survey and valuation costs (illustrative). */
    public static final long STANDARD_FEES = 3_500L;
    /** Rough gross-to-net ratio for an Irish PAYE earner, for the repayment-burden indicator only. */
    static final double NET_INCOME_RATIO = 0.72;
    static final long STEP = 1_000L;
    static final long MAX_PRICE_SEARCH = 5_000_000L;

    private final StampDutyCalculator stampDuty;
    private final MortgageCalculator mortgage;
    private final HelpToBuyCalculator helpToBuy;

    public AffordabilityCalculator(StampDutyCalculator stampDuty, MortgageCalculator mortgage,
                                   HelpToBuyCalculator helpToBuy) {
        this.stampDuty = stampDuty;
        this.mortgage = mortgage;
        this.helpToBuy = helpToBuy;
    }

    public AffordabilityResult calculate(AffordabilityRequest req) {
        BuyerType type = req.buyerType();
        long maxLoanByIncome = Math.round(req.householdIncome() * type.getLoanToIncome());

        long bestPrice = 0;
        // Scan upwards: the Help to Buy price cap makes the cash requirement non-monotonic,
        // so a simple scan is safer than a binary search. 5,000 iterations is trivial.
        for (long p = STEP; p <= MAX_PRICE_SEARCH; p += STEP) {
            if (cashShortfall(p, req, maxLoanByIncome) <= 0) {
                bestPrice = p;
            }
        }

        long loan = loanFor(bestPrice, type, maxLoanByIncome);
        long duty = stampDuty.residential(bestPrice);
        long htb = helpToBuy.relief(bestPrice, type, req.newBuild());
        long fees = bestPrice > 0 ? STANDARD_FEES : 0;
        double monthly = mortgage.monthlyRepayment(loan, req.ratePct(), req.termYears());
        double stressed = mortgage.monthlyRepayment(loan, req.ratePct() + 2.0, req.termYears());
        double netMonthly = req.householdIncome() * NET_INCOME_RATIO / 12.0;
        double burden = netMonthly <= 0 ? 0 : (monthly + Math.max(0, req.monthlyDebts())) * 100.0 / netMonthly;

        String limiting;
        if (bestPrice == 0) {
            limiting = "Not enough income or savings";
        } else if (loan >= maxLoanByIncome - STEP) {
            limiting = "Income (loan-to-income " + type.getLoanToIncome() + "x)";
        } else {
            limiting = "Deposit (loan-to-value " + Math.round(type.getLoanToValue() * 100) + "%)";
        }

        return new AffordabilityResult(bestPrice, loan, maxLoanByIncome, bestPrice - loan, duty, fees, htb,
                Math.round(monthly), Math.round(stressed), Math.round(burden * 10) / 10.0, limiting);
    }

    /** Household income needed to borrow the maximum LTV mortgage on a given price. */
    public long incomeNeeded(long price, BuyerType type) {
        return Math.round(price * type.getLoanToValue() / type.getLoanToIncome());
    }

    /** Minimum cash a buyer needs on day one: deposit + stamp duty + fees - Help to Buy. */
    public long cashNeeded(long price, BuyerType type, boolean newBuild) {
        long deposit = price - Math.round(price * type.getLoanToValue());
        return deposit + stampDuty.residential(price) + STANDARD_FEES - helpToBuy.relief(price, type, newBuild);
    }

    private long loanFor(long price, BuyerType type, long maxLoanByIncome) {
        return Math.min(maxLoanByIncome, Math.round(price * type.getLoanToValue()));
    }

    private long cashShortfall(long price, AffordabilityRequest req, long maxLoanByIncome) {
        long loan = loanFor(price, req.buyerType(), maxLoanByIncome);
        long cashNeeded = price - loan + stampDuty.residential(price) + STANDARD_FEES;
        long cashAvailable = req.savings() + helpToBuy.relief(price, req.buyerType(), req.newBuild());
        return cashNeeded - cashAvailable;
    }
}
