package ie.propertyhub.domain;

/**
 * Irish counties with illustrative market reference data used by the valuation engine.
 *
 * <p>Figures are approximate, rounded sample values for demonstration only. They are not
 * official statistics. Replace them with a live feed (e.g. CSO RPPI, RTB rent index) in a real system.
 */
public enum County {
    DUBLIN("Dublin", "Leinster", 5200, 2450, 6.5),
    CORK("Cork", "Munster", 3400, 1950, 7.5),
    GALWAY("Galway", "Connacht", 3300, 1900, 8.0),
    LIMERICK("Limerick", "Munster", 2900, 1750, 8.5),
    WATERFORD("Waterford", "Munster", 2700, 1600, 9.0),
    KILDARE("Kildare", "Leinster", 3600, 2050, 6.0),
    MEATH("Meath", "Leinster", 3400, 1950, 6.5),
    WICKLOW("Wicklow", "Leinster", 4100, 2150, 5.5),
    LOUTH("Louth", "Leinster", 2900, 1750, 7.0),
    KILKENNY("Kilkenny", "Leinster", 2800, 1550, 7.0),
    WEXFORD("Wexford", "Leinster", 2600, 1500, 7.5),
    KERRY("Kerry", "Munster", 2700, 1450, 8.0),
    CLARE("Clare", "Munster", 2500, 1450, 8.0),
    MAYO("Mayo", "Connacht", 2100, 1300, 9.0),
    DONEGAL("Donegal", "Ulster", 1900, 1200, 9.5),
    SLIGO("Sligo", "Connacht", 2200, 1300, 8.5);

    private final String displayName;
    private final String province;
    private final int avgPricePerSqm;
    private final int avgRentTwoBed;
    private final double annualGrowthPct;

    County(String displayName, String province, int avgPricePerSqm, int avgRentTwoBed, double annualGrowthPct) {
        this.displayName = displayName;
        this.province = province;
        this.avgPricePerSqm = avgPricePerSqm;
        this.avgRentTwoBed = avgRentTwoBed;
        this.annualGrowthPct = annualGrowthPct;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getProvince() {
        return province;
    }

    public int getAvgPricePerSqm() {
        return avgPricePerSqm;
    }

    public int getAvgRentTwoBed() {
        return avgRentTwoBed;
    }

    public double getAnnualGrowthPct() {
        return annualGrowthPct;
    }
}
