package ie.propertyhub.web;

import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Demo-only fault injection so you can show alerts firing during the assignment demo.
 * Disabled unless {@code propertyhub.chaos.enabled=true}.
 */
@RestController
@RequestMapping("/api/chaos")
@ConditionalOnProperty(name = "propertyhub.chaos.enabled", havingValue = "true")
public class ChaosController {

    @GetMapping("/error")
    public Map<String, String> error() {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Injected failure for alert demo");
    }

    @GetMapping("/slow")
    public Map<String, Object> slow(@RequestParam(defaultValue = "1500") long ms) throws InterruptedException {
        long capped = Math.min(Math.max(ms, 0), 10_000);
        Thread.sleep(capped);
        return Map.of("sleptMs", capped);
    }
}
