package com.x.store.dto;

import jakarta.validation.constraints.Size;

public record ReviewMarketplaceRequest(
        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason) {
}