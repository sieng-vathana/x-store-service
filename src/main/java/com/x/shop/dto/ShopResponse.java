package com.x.shop.dto;

import java.time.LocalDateTime;

public record ShopResponse(
        Long id,
        Long businessId,
        String name,
        String code,
        Integer status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
