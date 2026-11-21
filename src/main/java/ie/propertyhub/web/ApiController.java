package ie.propertyhub.web;

import ie.propertyhub.calc.AffordabilityCalculator;
import ie.propertyhub.calc.AffordabilityRequest;
import ie.propertyhub.calc.AffordabilityResult;
import ie.propertyhub.calc.RentalYieldCalculator;
import ie.propertyhub.calc.ValuationEngine;
import ie.propertyhub.calc.ValuationResult;
import ie.propertyhub.calc.YieldResult;
import ie.propertyhub.domain.BerRating;
import ie.propertyhub.domain.County;
import ie.propertyhub.domain.ListingType;
import ie.propertyhub.domain.PropertyType;
import ie.propertyhub.service.CountyInsight;
import ie.propertyhub.service.ListingService;
import ie.propertyhub.service.ListingView;
import ie.propertyhub.service.SearchCriteria;
import ie.propertyhub.service.SortOption;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** JSON API used by integration tests, the smoke test in the pipeline, and any future mobile app. */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final ListingService listings;
    private final ValuationEngine valuation;
    private final AffordabilityCalculator affordability;
    private final RentalYieldCalculator yieldCalc;
    private final MeterRegistry registry;

    public ApiController(ListingService listings, ValuationEngine valuation, AffordabilityCalculator affordability,
                         RentalYieldCalculator yieldCalc, MeterRegistry registry) {
        this.listings = listings;
        this.valuation = valuation;
        this.affordability = affordability;
        this.yieldCalc = yieldCalc;
        this.registry = registry;
    }

    /** Request body for an instant valuation. */
    public record ValuationRequest(@NotNull County county, @NotNull PropertyType propertyType,
                                   @Min(0) @Max(10) int beds, @Min(20) @Max(2000) int floorAreaSqm,
                                   @NotNull BerRating ber, boolean newBuild) {
    }

    @GetMapping("/listings")
    public List<ListingView> search(@RequestParam(defaultValue = "SALE") ListingType type,
                                    @RequestParam(required = false) County county,
                                    @RequestParam(required = false) Long maxPrice,
                                    @RequestParam(required = false) Integer minBeds,
                                    @RequestParam(defaultValue = "NEWEST") SortOption sort) {
        return listings.search(new SearchCriteria(type, county, null, null, maxPrice, minBeds, null, false, false,
                null, sort));
    }

    @GetMapping("/listings/{id}")
    public ListingView listing(@PathVariable long id) {
        return listings.find(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
    }

    @PostMapping("/valuation")
    public ValuationResult value(@Valid @RequestBody ValuationRequest r) {
        registry.counter("propertyhub.valuations", "source", "api").increment();
        return valuation.value(r.county(), r.propertyType(), r.beds(), r.floorAreaSqm(), r.ber(), r.newBuild());
    }

    @PostMapping("/affordability")
    public AffordabilityResult affordability(@RequestBody AffordabilityRequest r) {
        registry.counter("propertyhub.affordability.checks", "buyer_type",
                r.buyerType().name().toLowerCase(java.util.Locale.ROOT)).increment();
        return affordability.calculate(r);
    }

    @GetMapping("/yield")
    public YieldResult yield(@RequestParam long price, @RequestParam long rent,
                             @RequestParam(defaultValue = "4.5") double rate,
                             @RequestParam(defaultValue = "25") int term) {
        return yieldCalc.analyse(price, rent, rate, term);
    }

    @GetMapping("/insights")
    public List<CountyInsight> insights() {
        return listings.insights();
    }
}
