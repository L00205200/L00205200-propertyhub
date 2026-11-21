package ie.propertyhub.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FmtTest {

    private final Fmt fmt = new Fmt();

    @Test
    void formatsEuro() {
        assertThat(fmt.eur(1_234_567L)).isEqualTo("€1,234,567");
        assertThat(fmt.eur(-5.6)).isEqualTo("-€6");
        assertThat(fmt.eur(null)).isEqualTo("€0");
    }

    @Test
    void formatsCompactAndPercent() {
        assertThat(fmt.compact(545_000L)).isEqualTo("€545k");
        assertThat(fmt.compact(1_150_000L)).isEqualTo("€1.15m");
        assertThat(fmt.compact(950L)).isEqualTo("€950");
        assertThat(fmt.pct(7.25)).isEqualTo("7.3%");
        assertThat(fmt.num(12_345)).isEqualTo("12,345");
    }
}
