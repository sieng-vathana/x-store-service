package com.x.store.service;

import com.x.store.dto.CreateStoreRequest;
import com.x.store.entity.Store;
import com.x.store.repository.StoreRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StoreServiceTest {

    @Test
    void createNormalizesCodeAndKeepsBusinessReference() {
        StoreRepository repository = mock(StoreRepository.class);
        when(repository.existsByBusinessIdAndCode(10L, "MAIN")).thenReturn(false);
        when(repository.save(any(Store.class))).thenAnswer(invocation -> {
            Store store = invocation.getArgument(0);
            store.setId(1L);
            return store;
        });
        StoreService service = new StoreService(repository);

        var response = service.create(new CreateStoreRequest(
                10L, "Main Store", " main ", "123 Main Street", null, "Near market",
                "Phnom Penh", null, "kh", null, "+85512345678", null, null, null, null, null));

        assertEquals(10L, response.businessId());
        assertEquals("MAIN", response.code());
        assertEquals("KH", response.countryCode());
    }
}
