package ie.propertyhub.calc;

import static org.assertj.core.api.Assertions.assertThat;

import ie.propertyhub.domain.BerRating;
import ie.propertyhub.domain.Listing;
import ie.propertyhub.repository.SampleData;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class DomainTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 1);

    @Test
    void berRatingHelpers() {
        assertThat(BerRating.B2.grade()).isEqualTo("B");
        assertThat(BerRating.A1.atLeast(BerRating.C1)).isTrue();
        assertThat(BerRating.E1.atLeast(BerRating.C1)).isFalse();
        assertThat(BerRating.A2.valueFactor()).isGreaterThan(BerRating.G.valueFactor());
        assertThat(BerRating.B1.rentFactor()).isGreaterThan(BerRating.D1.rentFactor());
    }

    @Test
    void sampleDataIsValid() {
        List<Listing> all = SampleData.listings(TODAY);
        assertThat(all).hasSizeGreaterThan(30);
        assertThat(all).allSatisfy(l -> {
            assertThat(l.price()).isPositive();
            assertThat(l.floorAreaSqm()).isPositive();
            assertThat(l.priceHistory()).isNotEmpty();
            assertThat(l.features()).isNotEmpty();
            assertThat(l.daysOnMarket(TODAY)).isNotNegative();
        });
    }

    @Test
    void priceReductionIsDetected() {
        Listing reduced = SampleData.listings(TODAY).stream().filter(Listing::isPriceReduced).findFirst()
                .orElseThrow();
        assertThat(reduced.reductionPct()).isPositive();
        assertThat(reduced.pricePerSqm()).isPositive();

        Listing notReduced = SampleData.listings(TODAY).stream().filter(l -> !l.isPriceReduced()).findFirst()
                .orElseThrow();
        assertThat(notReduced.reductionPct()).isZero();
    }
}
