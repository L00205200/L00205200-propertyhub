package ie.propertyhub.calc;

import static org.assertj.core.api.Assertions.assertThat;

import ie.propertyhub.domain.BuyerType;
import org.junit.jupiter.api.Test;

class AffordabilityCalculatorTest {

    private final AffordabilityCalculator calc = new AffordabilityCalculator(
            new StampDutyCalculator(), new MortgageCalculator(), new HelpToBuyCalculator());

    @Test
    void depositLimitsFirstTimeBuyerWithSmallSavings() {
        AffordabilityResult r = calc.calculate(
                new AffordabilityRequest(60_000, 50_000, 50_000, 0, BuyerType.FIRST_TIME, false, 3.75, 30));

        assertThat(r.maxLoanByIncome()).isEqualTo(440_000);      // 4 x 110k
        assertThat(r.maxPrice()).isEqualTo(422_000);
        assertThat(r.mortgage()).isEqualTo(379_800);             // 90% LTV
        assertThat(r.deposit() + r.stampDuty() + r.fees()).isLessThanOrEqualTo(50_000);
        assertThat(r.limitingFactor()).startsWith("Deposit");
        assertThat(r.stressedMonthlyRepayment()).isGreaterThan(r.monthlyRepayment());
    }

    @Test
    void helpToBuyRaisesBudgetForNewBuild() {
        AffordabilityRequest base = new AffordabilityRequest(60_000, 50_000, 30_000, 0,
                BuyerType.FIRST_TIME, false, 3.75, 30);
        AffordabilityRequest newBuild = new AffordabilityRequest(60_000, 50_000, 30_000, 0,
                BuyerType.FIRST_TIME, true, 3.75, 30);

        AffordabilityResult without = calc.calculate(base);
        AffordabilityResult with = calc.calculate(newBuild);

        assertThat(with.helpToBuy()).isPositive();
        assertThat(with.maxPrice()).isGreaterThan(without.maxPrice());
    }

    @Test
    void incomeLimitsSecondTimeBuyerWithLargeDeposit() {
        AffordabilityResult r = calc.calculate(
                new AffordabilityRequest(60_000, 50_000, 200_000, 300, BuyerType.SECOND_TIME, false, 3.75, 30));

        assertThat(r.maxLoanByIncome()).isEqualTo(385_000);      // 3.5 x 110k
        assertThat(r.mortgage()).isEqualTo(385_000);
        assertThat(r.limitingFactor()).startsWith("Income");
        assertThat(r.repaymentToIncomePct()).isPositive();
    }

    @Test
    void noIncomeMeansNoBudget() {
        AffordabilityResult r = calc.calculate(
                new AffordabilityRequest(0, 0, 0, 0, null, false, 0, 0));
        assertThat(r.maxPrice()).isZero();
        assertThat(r.limitingFactor()).isEqualTo("Not enough income or savings");
    }

    @Test
    void requestDefaultsAreApplied() {
        AffordabilityRequest r = new AffordabilityRequest(1, -5, 0, 0, null, false, 0, 0);
        assertThat(r.buyerType()).isEqualTo(BuyerType.FIRST_TIME);
        assertThat(r.ratePct()).isEqualTo(3.75);
        assertThat(r.termYears()).isEqualTo(30);
        assertThat(r.householdIncome()).isEqualTo(1);
    }

    @Test
    void incomeAndCashNeededForAListing() {
        assertThat(calc.incomeNeeded(400_000, BuyerType.FIRST_TIME)).isEqualTo(90_000);
        // 40k deposit + 4k stamp duty + 3.5k fees - 30k HTB
        assertThat(calc.cashNeeded(400_000, BuyerType.FIRST_TIME, true)).isEqualTo(17_500);
        assertThat(calc.cashNeeded(400_000, BuyerType.FIRST_TIME, false)).isEqualTo(47_500);
    }
}
