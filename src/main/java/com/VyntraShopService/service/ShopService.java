package com.VyntraShopService.service;

import com.VyntraShopService.dto.CreateShopRequest;
import com.VyntraShopService.dto.ShopResponse;
import com.VyntraShopService.entity.Shop;
import com.VyntraShopService.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ShopService {

    private static final int ACTIVE_STATUS = 1;

    private final ShopRepository shopRepository;

    @Transactional
    public ShopResponse create(CreateShopRequest request) {
        String code = normalizeCode(request.code());
        if (shopRepository.existsByBusinessIdAndCode(request.businessId(), code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Shop code already exists for this business");
        }

        Shop shop = Shop.builder()
                .businessId(request.businessId())
                .name(request.name().trim())
                .code(code)
                .status(ACTIVE_STATUS)
                .build();
        return toResponse(shopRepository.save(shop));
    }

    @Transactional(readOnly = true)
    public ShopResponse getById(Long id) {
        return shopRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shop not found"));
    }

    @Transactional(readOnly = true)
    public List<ShopResponse> getByBusiness(Long businessId) {
        return shopRepository.findAllByBusinessIdOrderByCreatedAtDesc(businessId).stream()
                .map(this::toResponse)
                .toList();
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private ShopResponse toResponse(Shop shop) {
        return new ShopResponse(
                shop.getId(),
                shop.getBusinessId(),
                shop.getName(),
                shop.getCode(),
                shop.getStatus(),
                shop.getCreatedAt(),
                shop.getUpdatedAt());
    }
}
