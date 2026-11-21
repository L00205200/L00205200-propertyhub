package ie.propertyhub.service;

/** Sort orders offered on the search page. */
public enum SortOption {
    NEWEST("Newest"),
    PRICE_ASC("Price: low to high"),
    PRICE_DESC("Price: high to low"),
    YIELD_DESC("Best rental yield"),
    DEAL_DESC("Best value vs estimate");

    private final String label;

    SortOption(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
