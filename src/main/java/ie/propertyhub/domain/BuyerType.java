package ie.propertyhub.domain;

/**
 * Buyer categories under the Central Bank of Ireland mortgage measures (in force since 1 January 2023).
 * <ul>
 *   <li>First-time buyers: loan-to-income 4x, loan-to-value 90%</li>
 *   <li>Second and subsequent buyers: loan-to-income 3.5x, loan-to-value 90%</li>
 *   <li>Buy-to-let: loan-to-value 70%</li>
 * </ul>
 * Lenders can exceed these limits for a share of their lending (allowances); this app models the base rules.
 */
public enum BuyerType {
    FIRST_TIME("First-time buyer", 4.0, 0.90),
    SECOND_TIME("Second or subsequent buyer", 3.5, 0.90),
    BUY_TO_LET("Buy-to-let investor", 3.5, 0.70);

    private final String displayName;
    private final double loanToIncome;
    private final double loanToValue;

    BuyerType(String displayName, double loanToIncome, double loanToValue) {
        this.displayName = displayName;
        this.loanToIncome = loanToIncome;
        this.loanToValue = loanToValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getLoanToIncome() {
        return loanToIncome;
    }

    public double getLoanToValue() {
        return loanToValue;
    }
}
