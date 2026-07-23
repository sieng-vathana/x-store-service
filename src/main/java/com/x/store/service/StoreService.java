package com.x.store.service;

import com.x.store.dto.CreateStoreRequest;
import com.x.store.dto.StoreResponse;
import com.x.store.entity.Store;
import com.x.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class StoreService {

    private static final int ACTIVE_STATUS = 1;

    private final StoreRepository storeRepository;

    @Transactional
    public StoreResponse create(CreateStoreRequest request) {
        String code = normalizeCode(request.code());
        if (storeRepository.existsByBusinessIdAndCode(request.businessId(), code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Store code already exists for this business");
        }

        Store store = Store.builder()
                .businessId(request.businessId())
                .name(request.name().trim())
                .code(code)
                .addressLine1(request.addressLine1().trim())
                .addressLine2(trimToNull(request.addressLine2()))
                .landmark(trimToNull(request.landmark()))
                .city(request.city().trim())
                .stateProvince(trimToNull(request.stateProvince()))
                .countryCode(request.countryCode().trim().toUpperCase(Locale.ROOT))
                .postalCode(trimToNull(request.postalCode()))
                .phone(trimToNull(request.phone()))
                .alternatePhone(trimToNull(request.alternatePhone()))
                .email(trimToNull(request.email()))
                .website(trimToNull(request.website()))
                .latitude(request.latitude())
                .longitude(request.longitude())
                .status(ACTIVE_STATUS)
                .build();
        return toResponse(storeRepository.save(store));
    }

    @Transactional(readOnly = true)
    public StoreResponse getById(Long id) {
        return storeRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
    }

    @Transactional(readOnly = true)
    public List<StoreResponse> getByBusiness(Long businessId) {
        return storeRepository.findAllByBusinessIdOrderByCreatedAtDesc(businessId).stream()
                .map(this::toResponse)
                .toList();
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private StoreResponse toResponse(Store store) {
        return new StoreResponse(
                store.getId(), store.getBusinessId(), store.getName(), store.getCode(),
                store.getAddressLine1(), store.getAddressLine2(), store.getLandmark(), store.getCity(),
                store.getStateProvince(), store.getCountryCode(), store.getPostalCode(), store.getPhone(),
                store.getAlternatePhone(), store.getEmail(), store.getWebsite(), store.getLatitude(),
                store.getLongitude(), store.getStatus(), store.getCreatedAt(), store.getUpdatedAt());
    }
}
