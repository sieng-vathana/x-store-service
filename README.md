# Vyntra Shop Service

This service owns Vyntra selling locations.

## Domain boundary

- `business-service` owns the business profile and its owner.
- One business can own many shops.
- `shop-service` owns each shop's identity and lifecycle.
- Existing `storeId` references in order and user-service code are treated as shop IDs for backward compatibility.
- Geographic gazetteer and geocoding data can later belong to a separate `location-service`; a shop is not renamed to a location.

## API

- `POST /api/v1/shops` creates a shop under a business.
- `GET /api/v1/shops/{id}` returns a shop.
- `GET /api/v1/shops?businessId={id}` lists the shops owned by a business.

Public clients should use the authenticated BFF endpoints. The BFF verifies business ownership before forwarding shop operations.
