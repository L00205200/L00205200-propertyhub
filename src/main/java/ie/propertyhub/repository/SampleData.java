package ie.propertyhub.repository;

import ie.propertyhub.domain.BerRating;
import ie.propertyhub.domain.County;
import ie.propertyhub.domain.Listing;
import ie.propertyhub.domain.ListingType;
import ie.propertyhub.domain.PriceEvent;
import ie.propertyhub.domain.PropertyType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Fictional demo listings across Ireland. Addresses are invented; prices are illustrative.
 */
public final class SampleData {

    private SampleData() {
    }

    private static final Object[][] ROWS = {
        // address, area, county, listing, type, price, beds, baths, sqm, ber, new, daysAgo, reducedFrom, walk, transit, agent
        {"14 Elm Park Grove", "Rathmines", County.DUBLIN, ListingType.SALE, PropertyType.TERRACED, 545_000L, 3, 2, 98, BerRating.C2, false, 21, 575_000L, 92, 88, "Liffey Estates"},
        {"Apt 22, The Granary", "Grand Canal Dock", County.DUBLIN, ListingType.SALE, PropertyType.APARTMENT, 465_000L, 2, 2, 78, BerRating.A3, false, 9, 0L, 97, 95, "Docklands Property"},
        {"7 Oakfield Drive", "Lucan", County.DUBLIN, ListingType.SALE, PropertyType.SEMI_DETACHED, 495_000L, 3, 3, 105, BerRating.B3, false, 34, 0L, 64, 58, "Westside Homes"},
        {"3 Seafield Close", "Clontarf", County.DUBLIN, ListingType.SALE, PropertyType.DETACHED, 1_150_000L, 5, 4, 210, BerRating.B2, false, 48, 1_250_000L, 78, 72, "Bayview Auctioneers"},
        {"12 Larch Hill Row", "Tallaght", County.DUBLIN, ListingType.SALE, PropertyType.TERRACED, 365_000L, 3, 1, 88, BerRating.D1, false, 15, 0L, 70, 80, "Westside Homes"},
        {"Unit 5, Meadowbrook", "Swords", County.DUBLIN, ListingType.SALE, PropertyType.SEMI_DETACHED, 480_000L, 3, 3, 110, BerRating.A2, true, 6, 0L, 55, 62, "Fingal New Homes"},
        {"Apt 4, Harbour Court", "Dun Laoghaire", County.DUBLIN, ListingType.RENT, PropertyType.APARTMENT, 2_600L, 2, 1, 70, BerRating.B1, false, 4, 0L, 90, 92, "Bayview Auctioneers"},
        {"9 Mount Street Lower", "Dublin 2", County.DUBLIN, ListingType.RENT, PropertyType.APARTMENT, 2_150L, 1, 1, 50, BerRating.C1, false, 2, 0L, 99, 97, "City Lettings"},
        {"21 Ashgrove Park", "Blanchardstown", County.DUBLIN, ListingType.RENT, PropertyType.SEMI_DETACHED, 3_050L, 3, 2, 100, BerRating.B3, false, 11, 0L, 60, 70, "Westside Homes"},

        {"6 Lee View Terrace", "Douglas", County.CORK, ListingType.SALE, PropertyType.SEMI_DETACHED, 395_000L, 3, 2, 108, BerRating.C1, false, 27, 0L, 72, 60, "Rebel County Realty"},
        {"Apt 11, The Quays", "Cork City Centre", County.CORK, ListingType.SALE, PropertyType.APARTMENT, 255_000L, 2, 1, 68, BerRating.C3, false, 63, 275_000L, 95, 85, "Rebel County Realty"},
        {"Coolbawn House", "Midleton", County.CORK, ListingType.SALE, PropertyType.DETACHED, 520_000L, 4, 3, 165, BerRating.B2, false, 19, 0L, 45, 40, "East Cork Estates"},
        {"14 Ballincollig Green", "Ballincollig", County.CORK, ListingType.SALE, PropertyType.SEMI_DETACHED, 425_000L, 4, 3, 118, BerRating.A2, true, 5, 0L, 58, 48, "Rebel County Realty"},
        {"2 Patrick's Hill", "Cork City", County.CORK, ListingType.RENT, PropertyType.TERRACED, 2_100L, 3, 1, 90, BerRating.D2, false, 7, 0L, 88, 75, "Cork Lettings"},

        {"18 Salthill Crescent", "Salthill", County.GALWAY, ListingType.SALE, PropertyType.SEMI_DETACHED, 445_000L, 3, 2, 104, BerRating.B3, false, 12, 0L, 82, 55, "Corrib Property"},
        {"Apt 3, Spanish Arch Mews", "Galway City", County.GALWAY, ListingType.SALE, PropertyType.APARTMENT, 289_000L, 2, 2, 72, BerRating.B2, false, 40, 299_000L, 96, 70, "Corrib Property"},
        {"Knocknacarra Lodge", "Knocknacarra", County.GALWAY, ListingType.SALE, PropertyType.DETACHED, 575_000L, 4, 3, 175, BerRating.A3, false, 30, 0L, 50, 40, "Atlantic Homes"},
        {"5 Newcastle Road", "Newcastle", County.GALWAY, ListingType.RENT, PropertyType.APARTMENT, 1_850L, 2, 1, 65, BerRating.C2, false, 3, 0L, 85, 60, "Corrib Lettings"},

        {"27 O'Connell Avenue", "Limerick City", County.LIMERICK, ListingType.SALE, PropertyType.TERRACED, 235_000L, 3, 1, 95, BerRating.E1, false, 55, 255_000L, 90, 70, "Shannon Side Estates"},
        {"11 Castletroy Heights", "Castletroy", County.LIMERICK, ListingType.SALE, PropertyType.SEMI_DETACHED, 335_000L, 3, 2, 112, BerRating.B3, false, 17, 0L, 60, 50, "Shannon Side Estates"},
        {"Apt 8, Riverpoint", "Limerick City", County.LIMERICK, ListingType.RENT, PropertyType.APARTMENT, 1_650L, 2, 2, 75, BerRating.B2, false, 6, 0L, 94, 72, "Treaty Lettings"},

        {"4 The Mall", "Waterford City", County.WATERFORD, ListingType.SALE, PropertyType.TERRACED, 199_000L, 3, 1, 92, BerRating.D2, false, 42, 0L, 88, 55, "Deise Property"},
        {"Tramore Bay View", "Tramore", County.WATERFORD, ListingType.SALE, PropertyType.BUNGALOW, 315_000L, 3, 2, 120, BerRating.C2, false, 25, 0L, 52, 35, "Deise Property"},

        {"16 Canal Walk", "Naas", County.KILDARE, ListingType.SALE, PropertyType.SEMI_DETACHED, 410_000L, 3, 3, 110, BerRating.A2, true, 8, 0L, 62, 58, "Kildare New Homes"},
        {"9 Station Road", "Maynooth", County.KILDARE, ListingType.RENT, PropertyType.TERRACED, 2_300L, 3, 2, 95, BerRating.C1, false, 5, 0L, 80, 78, "Kildare Lettings"},
        {"22 Boyne Meadows", "Navan", County.MEATH, ListingType.SALE, PropertyType.SEMI_DETACHED, 349_000L, 3, 2, 105, BerRating.B2, false, 22, 365_000L, 58, 45, "Royal Meath Estates"},
        {"Apt 2, Seapoint", "Bray", County.WICKLOW, ListingType.SALE, PropertyType.APARTMENT, 340_000L, 2, 1, 72, BerRating.C1, false, 14, 0L, 89, 85, "Garden County Homes"},
        {"Glencree Cottage", "Enniskerry", County.WICKLOW, ListingType.SALE, PropertyType.DETACHED, 750_000L, 4, 3, 180, BerRating.C3, false, 75, 795_000L, 40, 25, "Garden County Homes"},
        {"31 Market Street", "Drogheda", County.LOUTH, ListingType.SALE, PropertyType.TERRACED, 249_000L, 3, 1, 90, BerRating.D1, false, 18, 0L, 85, 75, "Wee County Realty"},
        {"5 Canal Square", "Kilkenny City", County.KILKENNY, ListingType.SALE, PropertyType.TERRACED, 265_000L, 3, 2, 96, BerRating.C2, false, 29, 0L, 87, 50, "Marble City Estates"},
        {"Slaney View", "Enniscorthy", County.WEXFORD, ListingType.SALE, PropertyType.BUNGALOW, 255_000L, 3, 2, 115, BerRating.D1, false, 38, 0L, 45, 30, "Model County Homes"},
        {"8 Lakes Road", "Killarney", County.KERRY, ListingType.SALE, PropertyType.DETACHED, 395_000L, 4, 3, 160, BerRating.B3, false, 33, 0L, 55, 35, "Kingdom Property"},
        {"14 Abbey Street", "Ennis", County.CLARE, ListingType.SALE, PropertyType.TERRACED, 219_000L, 3, 1, 90, BerRating.D2, false, 26, 0L, 86, 45, "Banner Estates"},
        {"Clew Bay Lodge", "Westport", County.MAYO, ListingType.SALE, PropertyType.DETACHED, 345_000L, 4, 3, 170, BerRating.B2, false, 44, 365_000L, 60, 30, "Western Way Property"},
        {"3 Main Street", "Letterkenny", County.DONEGAL, ListingType.SALE, PropertyType.TERRACED, 175_000L, 3, 1, 95, BerRating.D1, false, 51, 0L, 80, 40, "Hills Property"},
        {"6 Garavogue Place", "Sligo Town", County.SLIGO, ListingType.SALE, PropertyType.SEMI_DETACHED, 239_000L, 3, 2, 102, BerRating.C3, false, 20, 0L, 75, 45, "Yeats Country Estates"},
        {"Apt 1, Quay Street", "Sligo Town", County.SLIGO, ListingType.RENT, PropertyType.APARTMENT, 1_350L, 2, 1, 66, BerRating.C2, false, 9, 0L, 90, 50, "Yeats Country Lettings"},
    };

    public static List<Listing> listings(LocalDate today) {
        List<Listing> out = new ArrayList<>();
        for (Object[] r : ROWS) {
            String address = (String) r[0];
            String area = (String) r[1];
            County county = (County) r[2];
            ListingType listingType = (ListingType) r[3];
            PropertyType type = (PropertyType) r[4];
            long price = (Long) r[5];
            int beds = (Integer) r[6];
            int baths = (Integer) r[7];
            int sqm = (Integer) r[8];
            BerRating ber = (BerRating) r[9];
            boolean newBuild = (Boolean) r[10];
            int daysAgo = (Integer) r[11];
            long reducedFrom = (Long) r[12];
            int walk = (Integer) r[13];
            int transit = (Integer) r[14];
            String agent = (String) r[15];

            LocalDate listed = today.minusDays(daysAgo);
            List<PriceEvent> history = new ArrayList<>();
            if (reducedFrom > 0) {
                history.add(new PriceEvent(listed, reducedFrom, "Listed"));
                history.add(new PriceEvent(listed.plusDays(Math.max(1, daysAgo / 2)), price, "Price reduced"));
            } else {
                history.add(new PriceEvent(listed, price, "Listed"));
            }

            String title = beds + " bed " + type.getDisplayName().toLowerCase() + " in " + area;
            out.add(new Listing(null, title, address, area, county, listingType, type, price, beds, baths, sqm,
                    ber, newBuild, describe(type, beds, area, county, newBuild, listingType),
                    features(type, ber, newBuild, walk, transit), listed, history, agent, walk, transit));
        }
        return out;
    }

    static String describe(PropertyType type, int beds, String area, County county, boolean newBuild,
                           ListingType listingType) {
        String intent = listingType == ListingType.RENT ? "available to rent" : "for sale";
        String build = newBuild ? "A brand-new, A-rated " : "A well-presented ";
        return build + beds + "-bedroom " + type.getDisplayName().toLowerCase() + " " + intent + " in " + area
                + ", Co. " + county.getDisplayName() + ". Close to local schools, shops and transport links, "
                + "this home suits families, professionals and investors alike.";
    }

    static List<String> features(PropertyType type, BerRating ber, boolean newBuild, int walk, int transit) {
        List<String> f = new ArrayList<>();
        if (ber.bandIndex() <= 1) {
            f.add("Heat pump & high energy efficiency");
        }
        if (newBuild) {
            f.add("New build - Help to Buy eligible (first-time buyers)");
            f.add("10-year HomeBond structural guarantee");
        }
        if (type == PropertyType.APARTMENT) {
            f.add("Designated parking & secure bike storage");
        } else {
            f.add("Private rear garden");
            f.add("Off-street parking");
        }
        if (walk >= 80) {
            f.add("Walk to shops, cafes and amenities");
        }
        if (transit >= 75) {
            f.add("Excellent public transport (bus/DART/Luas/rail)");
        }
        f.add("Fibre broadband available");
        return f;
    }
}
