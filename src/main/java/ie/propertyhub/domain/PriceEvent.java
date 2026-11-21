package ie.propertyhub.domain;

import java.time.LocalDate;

/** A point in a listing's price history (listed, reduced, etc.). */
public record PriceEvent(LocalDate date, long price, String event) {
}
