package ie.propertyhub.calc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StampDutyCalculatorTest {

    private final StampDutyCalculator calc = new StampDutyCalculator();

    @ParameterizedTest(name = "price {0} -> duty {1}")
    @CsvSource({
        "0, 0",
        "-5, 0",
        "400000, 4000",
        "1000000, 10000",
        "1200000, 14000",
        "1500000, 20000",
        "2000000, 50000"
    })
    void appliesIrishResidentialBands(long price, long expected) {
        assertThat(calc.residential(price)).isEqualTo(expected);
    }
}
