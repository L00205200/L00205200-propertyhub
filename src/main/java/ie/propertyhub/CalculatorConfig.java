package ie.propertyhub;

import ie.propertyhub.calc.AffordabilityCalculator;
import ie.propertyhub.calc.HelpToBuyCalculator;
import ie.propertyhub.calc.MortgageCalculator;
import ie.propertyhub.calc.RentalYieldCalculator;
import ie.propertyhub.calc.StampDutyCalculator;
import ie.propertyhub.calc.ValuationEngine;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** The calculators are plain Java (easy to unit test); this wires them as Spring beans. */
@Configuration
public class CalculatorConfig {

    @Bean
    public StampDutyCalculator stampDutyCalculator() {
        return new StampDutyCalculator();
    }

    @Bean
    public MortgageCalculator mortgageCalculator() {
        return new MortgageCalculator();
    }

    @Bean
    public HelpToBuyCalculator helpToBuyCalculator() {
        return new HelpToBuyCalculator();
    }

    @Bean
    public AffordabilityCalculator affordabilityCalculator(StampDutyCalculator s, MortgageCalculator m,
                                                           HelpToBuyCalculator h) {
        return new AffordabilityCalculator(s, m, h);
    }

    @Bean
    public ValuationEngine valuationEngine() {
        return new ValuationEngine();
    }

    @Bean
    public RentalYieldCalculator rentalYieldCalculator(MortgageCalculator m, StampDutyCalculator s) {
        return new RentalYieldCalculator(m, s);
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
