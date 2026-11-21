package ie.propertyhub.calc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class MortgageCalculatorTest {

    private final MortgageCalculator calc = new MortgageCalculator();

    @Test
    void standardAnnuityRepayment() {
        // EUR 300k over 30 years at 3.75% is about EUR 1,389 a month
        assertThat(calc.monthlyRepayment(300_000, 3.75, 30)).isCloseTo(1389.35, within(0.5));
    }

    @Test
    void zeroRateIsStraightLine() {
        assertThat(calc.monthlyRepayment(120_000, 0, 10)).isCloseTo(1000.0, within(0.001));
    }

    @Test
    void invalidInputsGiveZero() {
        assertThat(calc.monthlyRepayment(0, 4, 30)).isZero();
        assertThat(calc.monthlyRepayment(100_000, 4, 0)).isZero();
    }

    @Test
    void totalInterestIsPositive() {
        assertThat(calc.totalInterest(300_000, 3.75, 30)).isCloseTo(200_166, within(200.0));
    }
}
