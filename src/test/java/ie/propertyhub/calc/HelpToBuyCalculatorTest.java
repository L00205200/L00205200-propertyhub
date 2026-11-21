package ie.propertyhub.calc;

import static org.assertj.core.api.Assertions.assertThat;

import ie.propertyhub.domain.BuyerType;
import org.junit.jupiter.api.Test;

class HelpToBuyCalculatorTest {

    private final HelpToBuyCalculator htb = new HelpToBuyCalculator();

    @Test
    void tenPercentCappedAtThirtyThousand() {
        assertThat(htb.relief(250_000, BuyerType.FIRST_TIME, true)).isEqualTo(25_000);
        assertThat(htb.relief(450_000, BuyerType.FIRST_TIME, true)).isEqualTo(30_000);
    }

    @Test
    void notAvailableAbovePriceCap() {
        assertThat(htb.relief(500_000, BuyerType.FIRST_TIME, true)).isEqualTo(30_000);
        assertThat(htb.relief(500_001, BuyerType.FIRST_TIME, true)).isZero();
    }

    @Test
    void onlyFirstTimeBuyersOfNewBuilds() {
        assertThat(htb.relief(300_000, BuyerType.SECOND_TIME, true)).isZero();
        assertThat(htb.relief(300_000, BuyerType.FIRST_TIME, false)).isZero();
        assertThat(htb.relief(0, BuyerType.FIRST_TIME, true)).isZero();
    }
}
