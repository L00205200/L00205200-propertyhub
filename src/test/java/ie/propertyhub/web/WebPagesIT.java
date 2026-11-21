package ie.propertyhub.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests: full Spring context, every page rendered through Thymeleaf. */
@SpringBootTest
@AutoConfigureMockMvc
class WebPagesIT {

    @Autowired
    private MockMvc mvc;

    @Test
    void homePageRendersWithSecurityHeaders() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Best value right now")))
                .andExpect(content().string(containsString("Highest-yield counties")))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    void searchFiltersRender() throws Exception {
        mvc.perform(get("/search").param("type", "SALE").param("county", "DUBLIN").param("minBeds", "3")
                        .param("sort", "PRICE_ASC"))
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attributeExists("results"))
                .andExpect(content().string(containsString("in Co. Dublin")));
    }

    @Test
    void searchWithEmptyOptionalParams() throws Exception {
        mvc.perform(get("/search").param("type", "RENT").param("county", "").param("maxPrice", "")
                        .param("minBer", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("properties to rent")));
    }

    @Test
    void listingDetailShowsAnalytics() throws Exception {
        mvc.perform(get("/listing/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("PropertyHub estimate")))
                .andExpect(content().string(containsString("Rental potential")))
                .andExpect(content().string(containsString("Can I afford it?")))
                .andExpect(content().string(containsString("Mortgage calculator")));
    }

    @Test
    void rentalListingDetail() throws Exception {
        mvc.perform(get("/listing/7"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Can I afford the rent?")));
    }

    @Test
    void unknownListingIs404() throws Exception {
        mvc.perform(get("/listing/99999")).andExpect(status().isNotFound());
    }

    @Test
    void enquiryRedirectsWithFlashMessage() throws Exception {
        mvc.perform(post("/listing/1/enquiry").param("name", "Aoife <script>").param("email", "a@b.ie"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/listing/*"))
                .andExpect(flash().attribute("message", containsString("Aoife")));
    }

    @Test
    void affordabilityCalculates() throws Exception {
        mvc.perform(get("/affordability").param("calc", "true").param("income1", "60000")
                        .param("income2", "50000").param("savings", "50000").param("buyerType", "FIRST_TIME"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("result", "matches"))
                .andExpect(content().string(containsString("€422,000")));
    }

    @Test
    void affordabilityFormOnly() throws Exception {
        mvc.perform(get("/affordability"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("result"));
    }

    @Test
    void yieldCalculator() throws Exception {
        mvc.perform(get("/yield-calculator").param("price", "300000").param("rent", "1800"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("7.2%")));
    }

    @Test
    void sellEstimateAndPublish() throws Exception {
        mvc.perform(get("/sell")).andExpect(status().isOk());

        mvc.perform(post("/sell/estimate").param("address", "1 Test Lane").param("area", "Ranelagh")
                        .param("county", "DUBLIN").param("propertyType", "TERRACED").param("ber", "B2")
                        .param("listingType", "SALE").param("beds", "3").param("baths", "2")
                        .param("floorAreaSqm", "100"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("valuation"));

        mvc.perform(post("/sell").param("address", "1 Test Lane").param("area", "Ranelagh")
                        .param("county", "DUBLIN").param("propertyType", "TERRACED").param("ber", "B2")
                        .param("listingType", "SALE").param("beds", "3").param("baths", "2")
                        .param("floorAreaSqm", "100").param("askingPrice", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/listing/*"));
    }

    @Test
    void sellValidationErrors() throws Exception {
        mvc.perform(post("/sell").param("address", "").param("area", "").param("county", "CORK")
                        .param("propertyType", "TERRACED").param("ber", "C1").param("listingType", "RENT")
                        .param("beds", "0").param("baths", "1").param("floorAreaSqm", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("sell"))
                .andExpect(model().attributeHasFieldErrors("sellForm", "address", "area", "beds", "floorAreaSqm"));
    }

    @Test
    void assignmentBannerOnEveryPage() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("L00205200")))
                .andExpect(content().string(containsString("About this project")));
    }

    @Test
    void aboutPage() throws Exception {
        mvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"))
                .andExpect(content().string(containsString("Atlantic Technological University (ATU)")))
                .andExpect(content().string(containsString("Task 3 - CI/CD Pipeline")))
                .andExpect(content().string(containsString("L00205200")));
    }

    @Test
    void insightsPage() throws Exception {
        mvc.perform(get("/insights"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Leinster")));
    }
}
