package ie.propertyhub.web;

import java.text.NumberFormat;
import java.util.Locale;
import org.springframework.stereotype.Component;

/** Formatting helpers used from Thymeleaf templates as {@code ${@fmt.eur(x)}}. */
@Component("fmt")
public class Fmt {

    /** Single Number-typed method (no overloads) so SpEL resolution is unambiguous. */
    public String eur(Number value) {
        long amount = value == null ? 0 : Math.round(value.doubleValue());
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.UK);
        return (amount < 0 ? "-€" : "€") + nf.format(Math.abs(amount));
    }

    public String num(Number value) {
        return NumberFormat.getIntegerInstance(Locale.UK).format(value == null ? 0 : value.longValue());
    }

    public String pct(Number value) {
        return String.format(Locale.UK, "%.1f%%", value == null ? 0.0 : value.doubleValue());
    }

    /** Compact price, e.g. EUR 545k or EUR 1.15m. */
    public String compact(Number value) {
        long amount = value == null ? 0 : value.longValue();
        if (amount >= 1_000_000) {
            return String.format(Locale.UK, "€%.2fm", amount / 1_000_000.0);
        }
        if (amount >= 10_000) {
            return "€" + Math.round(amount / 1000.0) + "k";
        }
        return eur(amount);
    }
}
