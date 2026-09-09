package com.x.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApplyMarketplaceRequest(
        @NotBlank(message = "Store type is required")
        @Size(max = 64, message = "Store type must not exceed 64 characters")
        String storeType,
        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String note) {
}