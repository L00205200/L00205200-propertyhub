package ie.propertyhub.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * A property listing. For {@link ListingType#SALE} the price is the asking price in euro;
 * for {@link ListingType#RENT} it is the monthly rent in euro.
 */
public record Listing(
        Long id,
        String title,
        String address,
        String area,
        County county,
        ListingType listingType,
        PropertyType propertyType,
        long price,
        int beds,
        int baths,
        int floorAreaSqm,
        BerRating ber,
        boolean newBuild,
        String description,
        List<String> features,
        LocalDate listedDate,
        List<PriceEvent> priceHistory,
        String agent,
        int walkScore,
        int transitScore) {

    public Listing {
        features = features == null ? List.of() : List.copyOf(features);
        priceHistory = priceHistory == null ? List.of() : List.copyOf(priceHistory);
    }

    public Listing withId(Long newId) {
        return new Listing(newId, title, address, area, county, listingType, propertyType, price, beds, baths,
                floorAreaSqm, ber, newBuild, description, features, listedDate, priceHistory, agent, walkScore,
                transitScore);
    }

    public boolean isForSale() {
        return listingType == ListingType.SALE;
    }

    public long pricePerSqm() {
        return floorAreaSqm <= 0 ? 0 : Math.round((double) price / floorAreaSqm);
    }

    public long daysOnMarket(LocalDate today) {
        return listedDate == null ? 0 : Math.max(0, ChronoUnit.DAYS.between(listedDate, today));
    }

    /** True when the current price is lower than the first recorded price. */
    public boolean isPriceReduced() {
        return !priceHistory.isEmpty() && price < priceHistory.get(0).price();
    }

    /** Percentage reduction versus the original listed price (0 if none). */
    public double reductionPct() {
        if (!isPriceReduced()) {
            return 0;
        }
        long original = priceHistory.get(0).price();
        return (original - price) * 100.0 / original;
    }
}
