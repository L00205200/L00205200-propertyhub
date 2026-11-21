package ie.propertyhub.web;

import ie.propertyhub.domain.BerRating;
import ie.propertyhub.domain.County;
import ie.propertyhub.domain.Listing;
import ie.propertyhub.domain.ListingType;
import ie.propertyhub.domain.PriceEvent;
import ie.propertyhub.domain.PropertyType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/** Form backing object for "Sell or let your property". */
public class SellForm {

    @NotBlank
    @Size(max = 120)
    private String address;

    @NotBlank
    @Size(max = 60)
    private String area;

    @NotNull
    private County county = County.DUBLIN;

    @NotNull
    private ListingType listingType = ListingType.SALE;

    @NotNull
    private PropertyType propertyType = PropertyType.SEMI_DETACHED;

    @Min(1)
    @Max(10)
    private int beds = 3;

    @Min(1)
    @Max(10)
    private int baths = 2;

    @Min(20)
    @Max(2000)
    private int floorAreaSqm = 100;

    @NotNull
    private BerRating ber = BerRating.C1;

    private boolean newBuild;

    /** Optional: when empty the PropertyHub estimate is used. */
    @Min(1)
    private Long askingPrice;

    @Size(max = 2000)
    private String description;

    @Size(max = 80)
    private String agent = "Private seller";

    public Listing toListing(long price, LocalDate today) {
        String title = beds + " bed " + propertyType.getDisplayName().toLowerCase() + " in " + area;
        String desc = description == null || description.isBlank()
                ? "Listed by owner on PropertyHub Ireland." : description;
        return new Listing(null, title, address, area, county, listingType, propertyType, price, beds, baths,
                floorAreaSqm, ber, newBuild, desc, List.of(), today, List.of(new PriceEvent(today, price, "Listed")),
                agent == null || agent.isBlank() ? "Private seller" : agent, 50, 50);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public County getCounty() {
        return county;
    }

    public void setCounty(County county) {
        this.county = county;
    }

    public ListingType getListingType() {
        return listingType;
    }

    public void setListingType(ListingType listingType) {
        this.listingType = listingType;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public int getBeds() {
        return beds;
    }

    public void setBeds(int beds) {
        this.beds = beds;
    }

    public int getBaths() {
        return baths;
    }

    public void setBaths(int baths) {
        this.baths = baths;
    }

    public int getFloorAreaSqm() {
        return floorAreaSqm;
    }

    public void setFloorAreaSqm(int floorAreaSqm) {
        this.floorAreaSqm = floorAreaSqm;
    }

    public BerRating getBer() {
        return ber;
    }

    public void setBer(BerRating ber) {
        this.ber = ber;
    }

    public boolean isNewBuild() {
        return newBuild;
    }

    public void setNewBuild(boolean newBuild) {
        this.newBuild = newBuild;
    }

    public Long getAskingPrice() {
        return askingPrice;
    }

    public void setAskingPrice(Long askingPrice) {
        this.askingPrice = askingPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }
}
