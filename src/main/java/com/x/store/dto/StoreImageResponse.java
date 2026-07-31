package com.x.store.dto;

public record StoreImageResponse(Long id, String imageUrl, Boolean isPrimary, Integer sortOrder) {
}
