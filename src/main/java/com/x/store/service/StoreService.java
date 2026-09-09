package com.x.store.service;

import com.x.store.dto.CreateStoreRequest;
import com.x.store.dto.UpdateStoreRequest;
import com.x.store.dto.StoreResponse;
import com.x.store.dto.StoreImageResponse;
import com.x.store.entity.Store;
import com.x.store.entity.StoreImage;
import com.x.store.repository.StoreRepository;
import com.sharedlib.response.PageResponse;
import com.x.redis.cache.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Collection;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class StoreService {

    private static final int ACTIVE_STATUS = 1;

    private final StoreRepository storeRepository;

    @CacheEvict(cacheNames = CacheNames.STORES_BY_BUSINESS, allEntries = true)
    @Transactional
    public StoreResponse create(CreateStoreRequest request) {
        String code = normalizeCode(request.code());
        if (storeRepository.existsByBusinessIdAndCode(request.businessId(), code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Store code already exists for this business");
        }

        String storeType = request.storeType() != null && !request.storeType().isBlank()
                ? request.storeType().trim().toUpperCase(Locale.ROOT)
                : "GENERAL_RETAIL";

        Store store = Store.builder()
                .businessId(request.businessId())
                .name(request.name().trim())
                .code(code)
                .storeType(storeType)
                .marketplaceStatus("NOT_LISTED")
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
        prepareImages(store, request.images());
        return toResponse(storeRepository.save(store));
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.STORE_BY_ID, key = "#id"),
            @CacheEvict(cacheNames = CacheNames.STORES_BY_BUSINESS, allEntries = true)
    })
    @Transactional
    public StoreResponse update(Long id, UpdateStoreRequest request) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));

        if (request.name() != null && !request.name().isBlank()) {
            store.setName(request.name().trim());
        }
        if (request.code() != null && !request.code().isBlank()) {
            String code = normalizeCode(request.code());
            if (!code.equals(store.getCode()) && storeRepository.existsByBusinessIdAndCode(store.getBusinessId(), code)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Store code already exists for this business");
            }
            store.setCode(code);
        }
        if (request.addressLine1() != null && !request.addressLine1().isBlank()) {
            store.setAddressLine1(request.addressLine1().trim());
        }
        if (request.addressLine2() != null) {
            store.setAddressLine2(trimToNull(request.addressLine2()));
        }
        if (request.landmark() != null) {
            store.setLandmark(trimToNull(request.landmark()));
        }
        if (request.city() != null && !request.city().isBlank()) {
            store.setCity(request.city().trim());
        }
        if (request.stateProvince() != null) {
            store.setStateProvince(trimToNull(request.stateProvince()));
        }
        if (request.countryCode() != null && !request.countryCode().isBlank()) {
            store.setCountryCode(request.countryCode().trim().toUpperCase(Locale.ROOT));
        }
        if (request.postalCode() != null) {
            store.setPostalCode(trimToNull(request.postalCode()));
        }
        if (request.phone() != null) {
            store.setPhone(trimToNull(request.phone()));
        }
        if (request.alternatePhone() != null) {
            store.setAlternatePhone(trimToNull(request.alternatePhone()));
        }
        if (request.email() != null) {
            store.setEmail(trimToNull(request.email()));
        }
        if (request.website() != null) {
            store.setWebsite(trimToNull(request.website()));
        }
        if (request.latitude() != null) {
            store.setLatitude(request.latitude());
        }
        if (request.longitude() != null) {
            store.setLongitude(request.longitude());
        }
        if (request.status() != null) {
            store.setStatus(request.status());
        }
        if (request.storeType() != null && !request.storeType().isBlank()) {
            store.setStoreType(request.storeType().trim().toUpperCase(Locale.ROOT));
        }
        if (request.images() != null) {
            store.getImages().clear();
            prepareImages(store, request.images());
        }

        return toResponse(storeRepository.save(store));
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.STORE_BY_ID, key = "#id"),
            @CacheEvict(cacheNames = CacheNames.STORES_BY_BUSINESS, allEntries = true)
    })
    @Transactional
    public StoreResponse applyMarketplace(Long id, com.x.store.dto.ApplyMarketplaceRequest request) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        if (request != null && request.storeType() != null && !request.storeType().isBlank()) {
            store.setStoreType(request.storeType().trim().toUpperCase(Locale.ROOT));
        }
        store.setMarketplaceStatus("PENDING_REVIEW");
        store.setMarketplaceAppliedAt(java.time.LocalDateTime.now());
        store.setRejectionReason(null);
        return toResponse(storeRepository.save(store));
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.STORE_BY_ID, key = "#id"),
            @CacheEvict(cacheNames = CacheNames.STORES_BY_BUSINESS, allEntries = true)
    })
    @Transactional
    public StoreResponse approveMarketplace(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        store.setMarketplaceStatus("APPROVED");
        store.setMarketplaceApprovedAt(java.time.LocalDateTime.now());
        store.setRejectionReason(null);
        return toResponse(storeRepository.save(store));
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.STORE_BY_ID, key = "#id"),
            @CacheEvict(cacheNames = CacheNames.STORES_BY_BUSINESS, allEntries = true)
    })
    @Transactional
    public StoreResponse rejectMarketplace(Long id, com.x.store.dto.ReviewMarketplaceRequest request) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        store.setMarketplaceStatus("REJECTED");
        store.setRejectionReason(request != null ? trimToNull(request.reason()) : null);
        return toResponse(storeRepository.save(store));
    }

    @Transactional(readOnly = true)
    public PageResponse<StoreResponse> listPendingMarketplace(int page, int size) {
        var stores = storeRepository.findAllByMarketplaceStatus("PENDING_REVIEW",
                PageRequest.of(page, size, Sort.by("marketplaceAppliedAt").ascending()));
        return new PageResponse<>(stores.getContent().stream().map(this::toResponse).toList(),
                stores.getNumber(), stores.getSize(), stores.getTotalElements(), stores.getTotalPages(), stores.hasNext());
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.STORE_BY_ID, key = "#id"),
            @CacheEvict(cacheNames = CacheNames.STORES_BY_BUSINESS, allEntries = true)
    })
    @Transactional
    public void deleteFailedRegistration(Long id) {
        storeRepository.deleteById(id);
    }

    @Cacheable(cacheNames = CacheNames.STORE_BY_ID, key = "#id")
    @Transactional(readOnly = true)
    public StoreResponse getById(Long id) {
        return storeRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
    }

    @Transactional(readOnly = true)
    public List<com.x.store.dto.MarketplaceStoreResponse> getMarketplaceStores(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return storeRepository.findAllById(ids).stream()
                .filter(store -> (store.getStatus() == null || store.getStatus() == ACTIVE_STATUS)
                        && "APPROVED".equalsIgnoreCase(store.getMarketplaceStatus()))
                .map(store -> new com.x.store.dto.MarketplaceStoreResponse(
                        store.getId(), store.getName(), store.getCode(),
                        store.getStoreType() != null ? store.getStoreType() : "GENERAL_RETAIL",
                        store.getCity(), store.getCountryCode(),
                        store.getImages().stream()
                                .filter(image -> Boolean.TRUE.equals(image.getIsPrimary()))
                                .map(StoreImage::getImageUrl)
                                .findFirst()
                                .orElseGet(() -> store.getImages().stream()
                                        .map(StoreImage::getImageUrl)
                                        .findFirst()
                                        .orElse(null))))
                .toList();
    }

    @Cacheable(
            cacheNames = CacheNames.STORES_BY_BUSINESS,
            key = "#businessId + ':' + #page + ':' + #size")
    @Transactional(readOnly = true)
    public PageResponse<StoreResponse> getByBusiness(Long businessId, int page, int size) {
        var stores = storeRepository.findAllByBusinessId(businessId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return new PageResponse<>(stores.getContent().stream().map(this::toResponse).toList(),
                stores.getNumber(), stores.getSize(), stores.getTotalElements(), stores.getTotalPages(), stores.hasNext());
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

    private void prepareImages(Store store, List<com.x.store.dto.StoreImageRequest> imageRequests) {
        if (imageRequests == null || imageRequests.isEmpty()) {
            return;
        }
        long primaryCount = imageRequests.stream()
                .filter(image -> Boolean.TRUE.equals(image.isPrimary()))
                .count();
        if (primaryCount > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only one store image can be primary");
        }

        for (int index = 0; index < imageRequests.size(); index++) {
            var imageRequest = imageRequests.get(index);
            store.getImages().add(StoreImage.builder()
                    .store(store)
                    .imageUrl(imageRequest.imageUrl().trim())
                    .isPrimary(primaryCount == 0 ? index == 0 : Boolean.TRUE.equals(imageRequest.isPrimary()))
                    .sortOrder(imageRequest.sortOrder() == null ? index : imageRequest.sortOrder())
                    .build());
        }
    }

    private StoreResponse toResponse(Store store) {
        return new StoreResponse(
                store.getId(), store.getBusinessId(), store.getName(), store.getCode(),
                store.getStoreType() != null ? store.getStoreType() : "GENERAL_RETAIL",
                store.getAddressLine1(), store.getAddressLine2(), store.getLandmark(), store.getCity(),
                store.getStateProvince(), store.getCountryCode(), store.getPostalCode(), store.getPhone(),
                store.getAlternatePhone(), store.getEmail(), store.getWebsite(), store.getLatitude(),
                store.getLongitude(), store.getImages().stream()
                        .map(image -> new StoreImageResponse(image.getId(), image.getImageUrl(), image.getIsPrimary(), image.getSortOrder()))
                        .toList(), store.getStatus(),
                store.getMarketplaceStatus() != null ? store.getMarketplaceStatus() : "NOT_LISTED",
                store.getMarketplaceAppliedAt(), store.getMarketplaceApprovedAt(), store.getRejectionReason(),
                store.getCreatedAt(), store.getUpdatedAt());
    }
}
