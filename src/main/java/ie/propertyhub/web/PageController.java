package ie.propertyhub.web;

import ie.propertyhub.domain.BerRating;
import ie.propertyhub.domain.County;
import ie.propertyhub.domain.ListingType;
import ie.propertyhub.domain.PropertyType;
import ie.propertyhub.service.ListingService;
import ie.propertyhub.service.ListingView;
import ie.propertyhub.service.SearchCriteria;
import ie.propertyhub.service.SortOption;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Buyer and renter facing pages: home, search, listing detail. */
@Controller
public class PageController {

    private final ListingService listings;
    private final MeterRegistry registry;

    public PageController(ListingService listings, MeterRegistry registry) {
        this.listings = listings;
        this.registry = registry;
    }

    /** Lookup data used by the search form on several pages. */
    @ModelAttribute
    public void lookups(Model model) {
        model.addAttribute("counties", County.values());
        model.addAttribute("propertyTypes", PropertyType.values());
        model.addAttribute("berRatings", BerRating.values());
        model.addAttribute("sortOptions", SortOption.values());
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featured", listings.featured(6));
        model.addAttribute("insights", listings.insights().stream().limit(5).toList());
        return "index";
    }

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "SALE") ListingType type,
                         @RequestParam(required = false) County county,
                         @RequestParam(required = false) PropertyType propertyType,
                         @RequestParam(required = false) Long minPrice,
                         @RequestParam(required = false) Long maxPrice,
                         @RequestParam(required = false) Integer minBeds,
                         @RequestParam(required = false) BerRating minBer,
                         @RequestParam(defaultValue = "false") boolean newBuild,
                         @RequestParam(defaultValue = "false") boolean reduced,
                         @RequestParam(required = false) String q,
                         @RequestParam(defaultValue = "NEWEST") SortOption sort,
                         Model model) {
        SearchCriteria criteria = new SearchCriteria(type, county, propertyType, minPrice, maxPrice, minBeds, minBer,
                newBuild, reduced, q, sort);
        List<ListingView> results = listings.search(criteria);
        model.addAttribute("criteria", criteria);
        model.addAttribute("results", results);
        return "search";
    }

    @GetMapping("/listing/{id}")
    public String listing(@PathVariable long id, Model model) {
        ListingView view = listings.find(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
        model.addAttribute("v", view);
        model.addAttribute("l", view.listing());
        model.addAttribute("similar", listings.similar(view.listing(), 3));
        model.addAttribute("defaultRate", ListingService.DEFAULT_RATE);
        model.addAttribute("defaultTerm", ListingService.DEFAULT_TERM);
        return "listing";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @PostMapping("/listing/{id}/enquiry")
    public String enquiry(@PathVariable long id, @RequestParam String name, RedirectAttributes redirect) {
        listings.find(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
        registry.counter("propertyhub.enquiries").increment();
        String safeName = name == null ? "" : name.replaceAll("[^\\p{L} '\\-]", "");
        redirect.addFlashAttribute("message", "Thanks " + safeName + " - the agent will be in touch shortly.");
        return "redirect:/listing/" + id;
    }
}
