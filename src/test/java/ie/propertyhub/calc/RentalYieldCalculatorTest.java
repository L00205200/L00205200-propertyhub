package ie.propertyhub.calc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RentalYieldCalculatorTest {

    private final RentalYieldCalculator calc =
            new RentalYieldCalculator(new MortgageCalculator(), new StampDutyCalculator());

    @Test
    void grossAndNetYield() {
        YieldResult r = calc.analyse(300_000, 1_800, 4.5, 25);
        assertThat(r.annualRent()).isEqualTo(21_600);
        assertThat(r.grossYieldPct()).isEqualTo(7.2);
        assertThat(r.netYieldPct()).isEqualTo(5.7);
        assertThat(r.mortgage()).isEqualTo(210_000);   // 70% buy-to-let LTV
        assertThat(r.cashInvested()).isEqualTo(96_500); // 90k deposit + 3k stamp + 3.5k fees
        assertThat(r.rating()).isEqualTo("Strong");
    }

    @Test
    void invalidInputsReturnEmptyResult() {
        assertThat(calc.analyse(0, 1000, 4, 25).rating()).isEqualTo("n/a");
        assertThat(calc.analyse(100_000, 0, 4, 25).grossYieldPct()).isZero();
    }

    @Test
    void ratingBands() {
        assertThat(RentalYieldCalculator.rating(7.0)).isEqualTo("Strong");
        assertThat(RentalYieldCalculator.rating(6.0)).isEqualTo("Good");
        assertThat(RentalYieldCalculator.rating(4.5)).isEqualTo("Moderate");
        assertThat(RentalYieldCalculator.rating(3.0)).isEqualTo("Low");
    }
}
