package ie.propertyhub.web;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests for the JSON API, including the demo chaos endpoint. */
@SpringBootTest(properties = "propertyhub.chaos.enabled=true")
@AutoConfigureMockMvc
class ApiIT {

    @Autowired
    private MockMvc mvc;

    @Test
    void listListings() throws Exception {
        mvc.perform(get("/api/listings").param("county", "CORK").param("sort", "PRICE_DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThan(0)))
                .andExpect(jsonPath("$[0].listing.county").value("CORK"))
                .andExpect(jsonPath("$[0].valuation.estimate").isNumber());
    }

    @Test
    void getListingAnd404() throws Exception {
        mvc.perform(get("/api/listings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listing.id").value(1))
                .andExpect(jsonPath("$.rentalYield.grossYieldPct").isNumber());
        mvc.perform(get("/api/listings/424242")).andExpect(status().isNotFound());
    }

    @Test
    void valuation() throws Exception {
        String body = """
                {"county":"DUBLIN","propertyType":"SEMI_DETACHED","beds":3,"floorAreaSqm":110,"ber":"B2","newBuild":false}
                """;
        mvc.perform(post("/api/valuation").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimate").value(595000))
                .andExpect(jsonPath("$.monthlyRent").value(3130));
    }

    @Test
    void valuationValidation() throws Exception {
        String body = """
                {"county":"DUBLIN","propertyType":"SEMI_DETACHED","beds":3,"floorAreaSqm":5,"ber":"B2"}
                """;
        mvc.perform(post("/api/valuation").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void affordability() throws Exception {
        String body = """
                {"grossIncome1":60000,"grossIncome2":50000,"savings":50000,"buyerType":"FIRST_TIME"}
                """;
        mvc.perform(post("/api/affordability").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxPrice").value(422000))
                .andExpect(jsonPath("$.maxLoanByIncome").value(440000));
    }

    @Test
    void yieldAndInsights() throws Exception {
        mvc.perform(get("/api/yield").param("price", "300000").param("rent", "1800"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grossYieldPct").value(7.2));
        mvc.perform(get("/api/insights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(16));
    }

    @Test
    void chaosEndpointsForAlertDemo() throws Exception {
        mvc.perform(get("/api/chaos/error")).andExpect(status().isInternalServerError());
        mvc.perform(get("/api/chaos/slow").param("ms", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sleptMs").value(5));
    }
}
