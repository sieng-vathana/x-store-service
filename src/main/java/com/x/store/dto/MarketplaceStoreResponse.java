package com.x.store.dto;

public record MarketplaceStoreResponse(
        Long id,
        String name,
        String code,
        String city,
        String countryCode,
        String image) {
}
