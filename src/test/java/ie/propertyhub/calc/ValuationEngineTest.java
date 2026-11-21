package ie.propertyhub.calc;

import static org.assertj.core.api.Assertions.assertThat;

import ie.propertyhub.domain.BerRating;
import ie.propertyhub.domain.County;
import ie.propertyhub.domain.PropertyType;
import org.junit.jupiter.api.Test;

class ValuationEngineTest {

    private final ValuationEngine engine = new ValuationEngine();

    @Test
    void valuesDublinSemiAndRange() {
        ValuationResult r = engine.value(County.DUBLIN, PropertyType.SEMI_DETACHED, 3, 110, BerRating.B2, false);
        assertThat(r.estimate()).isEqualTo(595_000);
        assertThat(r.low()).isLessThan(r.estimate());
        assertThat(r.high()).isGreaterThan(r.estimate());
        assertThat(r.oneYearForecast()).isGreaterThan(r.estimate());
        assertThat(r.monthlyRent()).isEqualTo(3_130);
    }

    @Test
    void betterBerAndNewBuildIncreaseValue() {
        ValuationResult poor = engine.value(County.CORK, PropertyType.TERRACED, 3, 100, BerRating.G, false);
        ValuationResult good = engine.value(County.CORK, PropertyType.TERRACED, 3, 100, BerRating.A2, false);
        ValuationResult newBuild = engine.value(County.CORK, PropertyType.TERRACED, 3, 100, BerRating.A2, true);
        assertThat(good.estimate()).isGreaterThan(poor.estimate());
        assertThat(newBuild.estimate()).isGreaterThan(good.estimate());
    }

    @Test
    void moreBedroomsMoreRent() {
        long one = engine.monthlyRent(County.GALWAY, PropertyType.APARTMENT, 1, BerRating.C1);
        long two = engine.monthlyRent(County.GALWAY, PropertyType.APARTMENT, 2, BerRating.C1);
        long five = engine.monthlyRent(County.GALWAY, PropertyType.APARTMENT, 6, BerRating.C1);
        assertThat(one).isLessThan(two);
        assertThat(two).isLessThan(five);
        assertThat(two).isEqualTo(County.GALWAY.getAvgRentTwoBed());
    }

    @Test
    void zeroAreaIsSafe() {
        ValuationResult r = engine.value(County.MAYO, PropertyType.BUNGALOW, 3, 0, BerRating.C1, false);
        assertThat(r.estimate()).isZero();
        assertThat(r.pricePerSqm()).isZero();
    }

    @Test
    void bedMultipliersAreMonotonic() {
        assertThat(ValuationEngine.bedMultiplier(0)).isEqualTo(ValuationEngine.bedMultiplier(1));
        assertThat(ValuationEngine.bedMultiplier(3)).isLessThan(ValuationEngine.bedMultiplier(4));
        assertThat(ValuationEngine.bedMultiplier(4)).isLessThan(ValuationEngine.bedMultiplier(5));
    }
}
