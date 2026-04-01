package com.botomosy.srm1.web;

public class ProductCatalogItem {

    private final String code;
    private final String name;
    private final String shortDescription;
    private final String detailDescription;
    private final String priceText;

    public ProductCatalogItem(
            String code,
            String name,
            String shortDescription,
            String detailDescription,
            String priceText
    ) {
        this.code = code;
        this.name = name;
        this.shortDescription = shortDescription;
        this.detailDescription = detailDescription;
        this.priceText = priceText;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getDetailDescription() {
        return detailDescription;
    }

    public String getPriceText() {
        return priceText;
    }
}