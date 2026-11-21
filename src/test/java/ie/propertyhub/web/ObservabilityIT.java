package ie.propertyhub.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Starts the real embedded Tomcat on a random port and checks the endpoints that
 * Kubernetes probes and Prometheus scrape in production.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureObservability
class ObservabilityIT {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void healthAndProbesAreUp() {
        assertThat(rest.getForObject("/actuator/health", String.class)).contains("\"status\":\"UP\"");
        assertThat(rest.getForEntity("/actuator/health/liveness", String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(rest.getForEntity("/actuator/health/readiness", String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void prometheusExposesBusinessAndHttpMetrics() {
        rest.getForEntity("/search?type=SALE", String.class);
        ResponseEntity<String> metrics = rest.getForEntity("/actuator/prometheus", String.class);
        assertThat(metrics.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(metrics.getBody())
                .contains("propertyhub_searches_total")
                .contains("propertyhub_listings_active")
                .contains("http_server_requests_seconds_bucket")
                .contains("application=\"propertyhub\"");
    }

    @Test
    void infoEndpoint() {
        assertThat(rest.getForObject("/actuator/info", String.class))
                .contains("PropertyHub Ireland")
                .contains("L00205200");
    }
}
