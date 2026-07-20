package com.x.shop.service;

import com.x.shop.dto.CreateShopRequest;
import com.x.shop.entity.Shop;
import com.x.shop.repository.ShopRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShopServiceTest {

    @Test
    void createNormalizesCodeAndKeepsBusinessReference() {
        ShopRepository repository = mock(ShopRepository.class);
        when(repository.existsByBusinessIdAndCode(10L, "MAIN")).thenReturn(false);
        when(repository.save(any(Shop.class))).thenAnswer(invocation -> {
            Shop shop = invocation.getArgument(0);
            shop.setId(1L);
            return shop;
        });
        ShopService service = new ShopService(repository);

        var response = service.create(new CreateShopRequest(10L, "Main Shop", " main "));

        assertEquals(10L, response.businessId());
        assertEquals("MAIN", response.code());
    }
}
