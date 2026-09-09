package com.x.store.dto;

public record MarketplaceStoreResponse(
        Long id,
        String name,
        String code,
        String storeType,
        String city,
        String countryCode,
        String image) {

    public MarketplaceStoreResponse(Long id, String name, String code, String city, String countryCode, String image) {
        this(id, name, code, "GENERAL_RETAIL", city, countryCode, image);
    }
}
