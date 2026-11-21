package ie.propertyhub.web;

import ie.propertyhub.calc.AffordabilityRequest;
import ie.propertyhub.domain.BuyerType;

/** Query-string backed form for the affordability calculator. */
public class AffordabilityForm {

    private long income1 = 55_000;
    private long income2;
    private long savings = 40_000;
    private long monthlyDebts;
    private BuyerType buyerType = BuyerType.FIRST_TIME;
    private boolean newBuild;
    private double rate = 3.75;
    private int term = 30;

    public AffordabilityRequest toRequest() {
        return new AffordabilityRequest(income1, income2, savings, monthlyDebts, buyerType, newBuild, rate, term);
    }

    public long getIncome1() {
        return income1;
    }

    public void setIncome1(long income1) {
        this.income1 = income1;
    }

    public long getIncome2() {
        return income2;
    }

    public void setIncome2(long income2) {
        this.income2 = income2;
    }

    public long getSavings() {
        return savings;
    }

    public void setSavings(long savings) {
        this.savings = savings;
    }

    public long getMonthlyDebts() {
        return monthlyDebts;
    }

    public void setMonthlyDebts(long monthlyDebts) {
        this.monthlyDebts = monthlyDebts;
    }

    public BuyerType getBuyerType() {
        return buyerType;
    }

    public void setBuyerType(BuyerType buyerType) {
        this.buyerType = buyerType;
    }

    public boolean isNewBuild() {
        return newBuild;
    }

    public void setNewBuild(boolean newBuild) {
        this.newBuild = newBuild;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }
}
