package com.x.store.dto;

public record MarketplaceStoreResponse(
        Long id,
        String name,
        String code,
        String storeType,
        String city,
        String countryCode,
        String image,
        StoreOpeningHours openingHours,
        Long businessId) {

    public MarketplaceStoreResponse(Long id, String name, String code, String storeType, String city,
                                    String countryCode, String image, StoreOpeningHours openingHours) {
        this(id, name, code, storeType, city, countryCode, image, openingHours, null);
    }

    public MarketplaceStoreResponse(Long id, String name, String code, String city, String countryCode, String image) {
        this(id, name, code, "GENERAL_RETAIL", city, countryCode, image, null);
    }
}
