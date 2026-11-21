package ie.propertyhub.web;

import ie.propertyhub.calc.AffordabilityCalculator;
import ie.propertyhub.calc.AffordabilityResult;
import ie.propertyhub.calc.RentalYieldCalculator;
import ie.propertyhub.calc.ValuationEngine;
import ie.propertyhub.calc.ValuationResult;
import ie.propertyhub.domain.BerRating;
import ie.propertyhub.domain.BuyerType;
import ie.propertyhub.domain.County;
import ie.propertyhub.domain.Listing;
import ie.propertyhub.domain.ListingType;
import ie.propertyhub.domain.PropertyType;
import ie.propertyhub.service.ListingService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Calculators and seller tools: affordability, rental yield, instant valuation, market insights. */
@Controller
public class ToolsController {

    private final AffordabilityCalculator affordability;
    private final RentalYieldCalculator yieldCalc;
    private final ValuationEngine valuation;
    private final ListingService listings;
    private final MeterRegistry registry;
    private final Clock clock;

    public ToolsController(AffordabilityCalculator affordability, RentalYieldCalculator yieldCalc,
                           ValuationEngine valuation, ListingService listings, MeterRegistry registry, Clock clock) {
        this.affordability = affordability;
        this.yieldCalc = yieldCalc;
        this.valuation = valuation;
        this.listings = listings;
        this.registry = registry;
        this.clock = clock;
    }

    @ModelAttribute
    public void lookups(Model model) {
        model.addAttribute("counties", County.values());
        model.addAttribute("propertyTypes", PropertyType.values());
        model.addAttribute("berRatings", BerRating.values());
        model.addAttribute("buyerTypes", BuyerType.values());
        model.addAttribute("listingTypes", ListingType.values());
    }

    @GetMapping("/affordability")
    public String affordability(@ModelAttribute("form") AffordabilityForm form,
                                @RequestParam(defaultValue = "false") boolean calc, Model model) {
        if (calc) {
            AffordabilityResult result = affordability.calculate(form.toRequest());
            registry.counter("propertyhub.affordability.checks", "buyer_type",
                    form.getBuyerType().name().toLowerCase(java.util.Locale.ROOT)).increment();
            model.addAttribute("result", result);
            model.addAttribute("matches", listings.affordable(result.maxPrice(), 6));
        }
        return "affordability";
    }

    @GetMapping("/yield-calculator")
    public String yieldCalculator(@RequestParam(defaultValue = "300000") long price,
                                  @RequestParam(defaultValue = "1800") long rent,
                                  @RequestParam(defaultValue = "4.5") double rate,
                                  @RequestParam(defaultValue = "25") int term,
                                  Model model) {
        model.addAttribute("price", price);
        model.addAttribute("rent", rent);
        model.addAttribute("rate", rate);
        model.addAttribute("term", term);
        model.addAttribute("result", yieldCalc.analyse(price, rent, rate, term));
        return "yield";
    }

    @GetMapping("/sell")
    public String sellForm(Model model) {
        model.addAttribute("sellForm", new SellForm());
        return "sell";
    }

    @PostMapping("/sell/estimate")
    public String estimate(@Valid @ModelAttribute("sellForm") SellForm form, BindingResult binding, Model model) {
        if (!binding.hasErrors()) {
            model.addAttribute("valuation", valueOf(form));
            registry.counter("propertyhub.valuations", "source", "web").increment();
        }
        return "sell";
    }

    @PostMapping("/sell")
    public String publish(@Valid @ModelAttribute("sellForm") SellForm form, BindingResult binding, Model model) {
        if (binding.hasErrors()) {
            return "sell";
        }
        ValuationResult v = valueOf(form);
        long fallback = form.getListingType() == ListingType.RENT ? v.monthlyRent() : v.estimate();
        long price = form.getAskingPrice() != null ? form.getAskingPrice() : fallback;
        Listing saved = listings.create(form.toListing(price, LocalDate.now(clock)));
        return "redirect:/listing/" + saved.id();
    }

    @GetMapping("/insights")
    public String insights(Model model) {
        model.addAttribute("insights", listings.insights());
        return "insights";
    }

    private ValuationResult valueOf(SellForm f) {
        return valuation.value(f.getCounty(), f.getPropertyType(), f.getBeds(), f.getFloorAreaSqm(), f.getBer(),
                f.isNewBuild());
    }
}
