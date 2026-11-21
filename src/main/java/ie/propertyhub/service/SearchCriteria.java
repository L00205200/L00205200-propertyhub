package ie.propertyhub.service;

import ie.propertyhub.domain.BerRating;
import ie.propertyhub.domain.County;
import ie.propertyhub.domain.ListingType;
import ie.propertyhub.domain.PropertyType;

/** Search filters. Any null field means "no filter". */
public record SearchCriteria(
        ListingType listingType,
        County county,
        PropertyType propertyType,
        Long minPrice,
        Long maxPrice,
        Integer minBeds,
        BerRating minBer,
        boolean newBuildOnly,
        boolean reducedOnly,
        String keyword,
        SortOption sort) {

    public SearchCriteria {
        if (listingType == null) {
            listingType = ListingType.SALE;
        }
        if (sort == null) {
            sort = SortOption.NEWEST;
        }
        keyword = keyword == null ? null : keyword.trim();
    }

    public static SearchCriteria forSale() {
        return new SearchCriteria(ListingType.SALE, null, null, null, null, null, null, false, false, null, null);
    }
}
