package ie.propertyhub.service;

import static org.assertj.core.api.Assertions.assertThat;

import ie.propertyhub.calc.AffordabilityCalculator;
import ie.propertyhub.calc.HelpToBuyCalculator;
import ie.propertyhub.calc.MortgageCalculator;
import ie.propertyhub.calc.RentalYieldCalculator;
import ie.propertyhub.calc.StampDutyCalculator;
import ie.propertyhub.calc.ValuationEngine;
import ie.propertyhub.domain.BerRating;
import ie.propertyhub.domain.County;
import ie.propertyhub.domain.Listing;
import ie.propertyhub.domain.ListingType;
import ie.propertyhub.domain.PropertyType;
import ie.propertyhub.repository.ListingRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListingServiceTest {

    private SimpleMeterRegistry registry;
    private ListingService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-01T10:00:00Z"), ZoneId.of("Europe/Dublin"));
        StampDutyCalculator stamp = new StampDutyCalculator();
        MortgageCalculator mortgage = new MortgageCalculator();
        registry = new SimpleMeterRegistry();
        service = new ListingService(new ListingRepository(clock), new ValuationEngine(),
                new RentalYieldCalculator(mortgage, stamp), stamp,
                new AffordabilityCalculator(stamp, mortgage, new HelpToBuyCalculator()), mortgage, clock, registry);
    }

    private static SearchCriteria criteria(ListingType type, County county, Long maxPrice, Integer minBeds,
                                           SortOption sort) {
        return new SearchCriteria(type, county, null, null, maxPrice, minBeds, null, false, false, null, sort);
    }

    @Test
    void filtersBySaleCountyAndPrice() {
        List<ListingView> results = service.search(criteria(ListingType.SALE, County.DUBLIN, 600_000L, 3, null));
        assertThat(results).isNotEmpty().allSatisfy(v -> {
            assertThat(v.listing().county()).isEqualTo(County.DUBLIN);
            assertThat(v.listing().price()).isLessThanOrEqualTo(600_000L);
            assertThat(v.listing().beds()).isGreaterThanOrEqualTo(3);
            assertThat(v.rentalYield()).isNotNull();
        });
        assertThat(registry.get("propertyhub.searches").tag("listing_type", "sale").counter().count())
                .isEqualTo(1.0);
    }

    @Test
    void sortsByPrice() {
        List<ListingView> asc = service.search(criteria(ListingType.SALE, null, null, null, SortOption.PRICE_ASC));
        List<ListingView> desc = service.search(criteria(ListingType.SALE, null, null, null, SortOption.PRICE_DESC));
        assertThat(asc.get(0).listing().price()).isLessThanOrEqualTo(asc.get(asc.size() - 1).listing().price());
        assertThat(desc.get(0).listing().price()).isGreaterThanOrEqualTo(desc.get(1).listing().price());
    }

    @Test
    void otherSortsAndFlags() {
        assertThat(service.search(criteria(ListingType.SALE, null, null, null, SortOption.YIELD_DESC))).isNotEmpty();
        assertThat(service.search(criteria(ListingType.SALE, null, null, null, SortOption.DEAL_DESC))).isNotEmpty();
        assertThat(service.search(new SearchCriteria(ListingType.SALE, null, null, null, null, null, null, true,
                false, null, null))).allMatch(v -> v.listing().newBuild());
        assertThat(service.search(new SearchCriteria(ListingType.SALE, null, null, null, null, null, null, false,
                true, null, null))).allMatch(v -> v.listing().isPriceReduced());
        assertThat(service.search(new SearchCriteria(ListingType.SALE, null, PropertyType.APARTMENT, 100_000L, null,
                null, BerRating.B3, false, false, null, null)))
                .allMatch(v -> v.listing().propertyType() == PropertyType.APARTMENT
                        && v.listing().ber().atLeast(BerRating.B3));
    }

    @Test
    void keywordSearch() {
        List<ListingView> results = service.search(new SearchCriteria(ListingType.SALE, null, null, null, null, null,
                null, false, false, "  rathmines ", null));
        assertThat(results).hasSize(1);
        assertThat(results.get(0).listing().area()).isEqualTo("Rathmines");
    }

    @Test
    void rentalListingsHaveNoYieldButHaveRentEstimate() {
        List<ListingView> rentals = service.search(criteria(ListingType.RENT, null, null, null, null));
        assertThat(rentals).isNotEmpty().allSatisfy(v -> {
            assertThat(v.rentalYield()).isNull();
            assertThat(v.valuation().monthlyRent()).isPositive();
        });
    }

    @Test
    void findSimilarFeaturedAndAffordable() {
        ListingView first = service.find(1).orElseThrow();
        assertThat(service.similar(first.listing(), 3)).isNotEmpty()
                .allMatch(v -> v.listing().county() == first.listing().county() && !v.listing().id().equals(1L));
        assertThat(service.featured(6)).hasSize(6);
        assertThat(service.affordable(300_000, 50)).allMatch(v -> v.listing().price() <= 300_000);
        assertThat(service.find(9999)).isEmpty();
    }

    @Test
    void createAddsListingAndIncrementsMetric() {
        LocalDate today = LocalDate.of(2026, 11, 1);
        Listing input = new Listing(42L, "t", "1 Test Road", "Testville", County.CLARE, ListingType.SALE,
                PropertyType.BUNGALOW, 250_000, 3, 1, 100, BerRating.C1, false, "d", null, today, null, "me", 1, 1);
        Listing saved = service.create(input);
        assertThat(saved.id()).isNotEqualTo(42L);
        assertThat(service.find(saved.id())).isPresent();
        assertThat(registry.get("propertyhub.listings.created").counter().count()).isEqualTo(1.0);
        assertThat(registry.get("propertyhub.listings.active").gauge().value()).isGreaterThan(30);
    }

    @Test
    void insightsCoverEveryCounty() {
        List<CountyInsight> insights = service.insights();
        assertThat(insights).hasSize(County.values().length);
        assertThat(insights.get(0).grossYieldPct()).isGreaterThanOrEqualTo(insights.get(1).grossYieldPct());
    }

    @Test
    void dealLabels() {
        assertThat(ListingService.dealLabel(6)).isEqualTo("Great price");
        assertThat(ListingService.dealLabel(1)).isEqualTo("Fair price");
        assertThat(ListingService.dealLabel(-3)).isEqualTo("Slightly above estimate");
        assertThat(ListingService.dealLabel(-12)).isEqualTo("Above estimate");
    }
}
