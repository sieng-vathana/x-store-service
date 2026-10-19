package com.x.redis.config;

import com.x.store.dto.StoreResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The Redis cache must read back what it wrote, including the computed suspended flag. */
class StoreResponseCacheRoundTripTest {

    @Test
    void suspendedStoreSurvivesTheRedisSerializer() {
        var serializer = XRedisAutoConfiguration.jsonSerializer();
        StoreResponse store = new StoreResponse(2L, 2L, "Shopify", "MAIN", "GENERAL_RETAIL", "Street 1", null, null,
                "Phnom Penh", null, "KH", null, null, null, null, null, null, null, List.of(), 1, "NOT_LISTED",
                null, null, null, LocalDateTime.now(), LocalDateTime.now(), null,
                LocalDateTime.of(2026, 10, 19, 10, 0), "Counterfeit goods", 9L);

        Object read = serializer.deserialize(serializer.serialize(store));

        assertEquals(store, read);
        assertEquals(true, ((StoreResponse) read).suspended());
    }
}
