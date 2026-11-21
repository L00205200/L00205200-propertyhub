package ie.propertyhub.service;

import ie.propertyhub.calc.AffordabilityCalculator;
import ie.propertyhub.calc.MortgageCalculator;
import ie.propertyhub.calc.RentalYieldCalculator;
import ie.propertyhub.calc.StampDutyCalculator;
import ie.propertyhub.calc.ValuationEngine;
import ie.propertyhub.calc.ValuationResult;
import ie.propertyhub.calc.YieldResult;
import ie.propertyhub.domain.BuyerType;
import ie.propertyhub.domain.County;
import ie.propertyhub.domain.Listing;
import ie.propertyhub.domain.ListingType;
import ie.propertyhub.repository.ListingRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Search, enrichment and creation of listings, with business metrics for Prometheus. */
@Service
public class ListingService {

    public static final double DEFAULT_RATE = 3.75;
    public static final int DEFAULT_TERM = 30;

    private static final Logger log = LoggerFactory.getLogger(ListingService.class);

    private final ListingRepository repository;
    private final ValuationEngine valuation;
    private final RentalYieldCalculator yieldCalc;
    private final StampDutyCalculator stampDuty;
    private final AffordabilityCalculator affordability;
    private final MortgageCalculator mortgage;
    private final Clock clock;
    private final MeterRegistry registry;
    private final Counter listingsCreated;

    public ListingService(ListingRepository repository, ValuationEngine valuation, RentalYieldCalculator yieldCalc,
                          StampDutyCalculator stampDuty, AffordabilityCalculator affordability,
                          MortgageCalculator mortgage, Clock clock, MeterRegistry registry) {
        this.repository = repository;
        this.valuation = valuation;
        this.yieldCalc = yieldCalc;
        this.stampDuty = stampDuty;
        this.affordability = affordability;
        this.mortgage = mortgage;
        this.clock = clock;
        this.registry = registry;
        this.listingsCreated = Counter.builder("propertyhub.listings.created")
                .description("Listings created through the Sell page or API").register(registry);
        Gauge.builder("propertyhub.listings.active", repository, ListingRepository::count)
                .description("Listings currently on the market").register(registry);
    }

    public List<ListingView> search(SearchCriteria c) {
        Counter.builder("propertyhub.searches").description("Property searches")
                .tag("listing_type", c.listingType().name().toLowerCase(Locale.ROOT))
                .register(registry).increment();

        Stream<ListingView> stream = repository.findAll().stream()
                .filter(l -> l.listingType() == c.listingType())
                .filter(l -> c.county() == null || l.county() == c.county())
                .filter(l -> c.propertyType() == null || l.propertyType() == c.propertyType())
                .filter(l -> c.minPrice() == null || l.price() >= c.minPrice())
                .filter(l -> c.maxPrice() == null || l.price() <= c.maxPrice())
                .filter(l -> c.minBeds() == null || l.beds() >= c.minBeds())
                .filter(l -> c.minBer() == null || l.ber().atLeast(c.minBer()))
                .filter(l -> !c.newBuildOnly() || l.newBuild())
                .filter(l -> !c.reducedOnly() || l.isPriceReduced())
                .filter(l -> matchesKeyword(l, c.keyword()))
                .map(this::enrich);

        Comparator<ListingView> order = switch (c.sort()) {
            case PRICE_ASC -> Comparator.comparingLong(v -> v.listing().price());
            case PRICE_DESC -> Comparator.comparingLong((ListingView v) -> v.listing().price()).reversed();
            case YIELD_DESC -> Comparator.comparingDouble(ListingService::grossYield).reversed();
            case DEAL_DESC -> Comparator.comparingDouble(ListingView::valueGapPct).reversed();
            case NEWEST -> Comparator.comparingLong(ListingView::daysOnMarket);
        };
        List<ListingView> results = stream.sorted(order).toList();
        log.info("search type={} county={} results={}", c.listingType(), c.county(), results.size());
        return results;
    }

    public Optional<ListingView> find(long id) {
        return repository.findById(id).map(this::enrich);
    }

    /** Newest sale listings with the best value-vs-estimate, for the home page. */
    public List<ListingView> featured(int limit) {
        return repository.findAll().stream()
                .filter(Listing::isForSale)
                .map(this::enrich)
                .sorted(Comparator.comparingDouble(ListingView::valueGapPct).reversed())
                .limit(limit)
                .toList();
    }

    /** Similar listings: same county and listing type, closest in price. */
    public List<ListingView> similar(Listing target, int limit) {
        return repository.findAll().stream()
                .filter(l -> !l.id().equals(target.id()))
                .filter(l -> l.county() == target.county() && l.listingType() == target.listingType())
                .sorted(Comparator.comparingLong(l -> Math.abs(l.price() - target.price())))
                .limit(limit)
                .map(this::enrich)
                .toList();
    }

    /** Sale listings a buyer can afford, best value first. */
    public List<ListingView> affordable(long maxPrice, int limit) {
        return repository.findAll().stream()
                .filter(Listing::isForSale)
                .filter(l -> l.price() <= maxPrice)
                .map(this::enrich)
                .sorted(Comparator.comparingLong((ListingView v) -> v.listing().price()).reversed())
                .limit(limit)
                .toList();
    }

    public Listing create(Listing listing) {
        Listing saved = repository.save(listing.withId(null));
        listingsCreated.increment();
        log.info("listing created id={} county={} price={}", saved.id(), saved.county(), saved.price());
        return saved;
    }

    public List<CountyInsight> insights() {
        List<Listing> all = repository.findAll();
        List<CountyInsight> out = new ArrayList<>();
        for (County county : County.values()) {
            List<Listing> sale = all.stream().filter(l -> l.county() == county && l.isForSale()).toList();
            long rent = all.stream().filter(l -> l.county() == county && l.listingType() == ListingType.RENT)
                    .count();
            long avgPrice = Math.round(sale.stream().mapToLong(Listing::price).average().orElse(0));
            double avgYield = sale.stream().map(this::enrich).mapToDouble(ListingService::grossYield)
                    .average().orElse(0);
            out.add(new CountyInsight(county, sale.size(), rent, avgPrice, county.getAvgPricePerSqm(),
                    county.getAvgRentTwoBed(), Math.round(avgYield * 10) / 10.0, county.getAnnualGrowthPct(),
                    affordability.incomeNeeded(avgPrice, BuyerType.FIRST_TIME)));
        }
        out.sort(Comparator.comparingDouble(CountyInsight::grossYieldPct).reversed());
        return out;
    }

    public ListingView enrich(Listing l) {
        ValuationResult v = valuation.value(l);
        LocalDate today = LocalDate.now(clock);
        if (l.isForSale()) {
            YieldResult y = yieldCalc.analyse(l.price(), v.monthlyRent(), DEFAULT_RATE + 0.75, 25);
            double gap = valuation.discountToEstimatePct(l);
            long loan = Math.round(l.price() * BuyerType.FIRST_TIME.getLoanToValue());
            return new ListingView(l, v, y, round1(gap), dealLabel(gap), stampDuty.residential(l.price()),
                    affordability.incomeNeeded(l.price(), BuyerType.FIRST_TIME),
                    affordability.cashNeeded(l.price(), BuyerType.FIRST_TIME, l.newBuild()),
                    Math.round(mortgage.monthlyRepayment(loan, DEFAULT_RATE, DEFAULT_TERM)),
                    l.daysOnMarket(today));
        }
        double gap = v.monthlyRent() == 0 ? 0 : (v.monthlyRent() - l.price()) * 100.0 / v.monthlyRent();
        return new ListingView(l, v, null, round1(gap), dealLabel(gap), 0, 0, 0, 0, l.daysOnMarket(today));
    }

    static String dealLabel(double gapPct) {
        if (gapPct >= 5) {
            return "Great price";
        }
        if (gapPct >= 0) {
            return "Fair price";
        }
        if (gapPct >= -8) {
            return "Slightly above estimate";
        }
        return "Above estimate";
    }

    private static double grossYield(ListingView v) {
        return v.rentalYield() == null ? 0 : v.rentalYield().grossYieldPct();
    }

    private static boolean matchesKeyword(Listing l, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String k = keyword.toLowerCase(Locale.ROOT);
        return Stream.of(l.title(), l.address(), l.area(), l.county().getDisplayName(), l.description())
                .anyMatch(s -> s != null && s.toLowerCase(Locale.ROOT).contains(k));
    }

    private static double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
