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
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
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
                .openingHours(validateOpeningHours(request.openingHours()))
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
        if (Boolean.TRUE.equals(request.clearOpeningHours())) {
            store.setOpeningHours(null);
        } else if (request.openingHours() != null) {
            store.setOpeningHours(validateOpeningHours(request.openingHours()));
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
        if ("APPROVED".equalsIgnoreCase(store.getMarketplaceStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Store is already approved for the marketplace");
        }
        if ("PENDING_REVIEW".equalsIgnoreCase(store.getMarketplaceStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Marketplace application is already under review");
        }
        if (request != null && request.storeType() != null && !request.storeType().isBlank()) {
            store.setStoreType(request.storeType().trim().toUpperCase(Locale.ROOT));
        }
        requireMarketplaceReadiness(store);
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
        if (!"PENDING_REVIEW".equalsIgnoreCase(store.getMarketplaceStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only applications awaiting review can be approved");
        }
        requireMarketplaceReadiness(store);
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

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.STORE_BY_ID, key = "#id"),
            @CacheEvict(cacheNames = CacheNames.STORES_BY_BUSINESS, allEntries = true)
    })
    @Transactional
    public StoreResponse requestMarketplaceChanges(Long id, com.x.store.dto.ReviewMarketplaceRequest request) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        if (!"PENDING_REVIEW".equalsIgnoreCase(store.getMarketplaceStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only applications awaiting review can be returned for changes");
        }
        String reason = request == null ? null : trimToNull(request.reason());
        if (reason == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Explain what the merchant needs to update");
        }
        store.setMarketplaceStatus("NEEDS_CHANGES");
        store.setRejectionReason(reason);
        return toResponse(storeRepository.save(store));
    }

    @Transactional(readOnly = true)
    public PageResponse<StoreResponse> listPendingMarketplace(int page, int size) {
        var stores = storeRepository.findAllByMarketplaceStatus("PENDING_REVIEW",
                PageRequest.of(page, size, Sort.by("marketplaceAppliedAt").ascending()));
        return new PageResponse<>(stores.getContent().stream().map(this::toResponse).toList(),
                stores.getNumber(), stores.getSize(), stores.getTotalElements(), stores.getTotalPages(), stores.hasNext());
    }

    @Transactional(readOnly = true)
    public PageResponse<StoreResponse> listMarketplaceNeedsChanges(int page, int size) {
        var stores = storeRepository.findAllByMarketplaceStatus("NEEDS_CHANGES",
                PageRequest.of(page, size, Sort.by("updatedAt").descending()));
        return new PageResponse<>(stores.getContent().stream().map(this::toResponse).toList(),
                stores.getNumber(), stores.getSize(), stores.getTotalElements(), stores.getTotalPages(), stores.hasNext());
    }

    private void requireMarketplaceReadiness(Store store) {
        var missing = new java.util.ArrayList<String>();
        if (isBlank(store.getName()) || isBlank(store.getStoreType())) missing.add("Store name and type");
        if (isBlank(store.getPhone()) && isBlank(store.getEmail())) missing.add("Contact phone or email");
        if (isBlank(store.getAddressLine1()) || isBlank(store.getCity())
                || isBlank(store.getStateProvince()) || isBlank(store.getCountryCode())) {
            missing.add("Full address (street, city, province, and country)");
        }
        if (!hasCompleteOpeningHours(store.getOpeningHours())) missing.add("Weekly opening hours");
        if (store.getImages() == null || store.getImages().stream().noneMatch(image -> !isBlank(image.getImageUrl()))) {
            missing.add("Storefront photo");
        }
        if (!missing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Marketplace listing is missing: " + String.join(", ", missing));
        }
    }

    private boolean hasCompleteOpeningHours(com.x.store.dto.StoreOpeningHours hours) {
        if (hours == null || hours.timeZone() == null || !"Asia/Phnom_Penh".equals(hours.timeZone().trim()) || hours.days() == null
                || !hours.days().keySet().equals(EnumSet.allOf(DayOfWeek.class))) {
            return false;
        }
        boolean hasOpenDay = false;
        for (DayOfWeek day : DayOfWeek.values()) {
            var schedule = hours.days().get(day);
            if (schedule == null) return false;
            if (schedule.closed()) continue;
            hasOpenDay = true;
            if (!isValidMarketplaceTime(schedule.openTime()) || !isValidMarketplaceTime(schedule.closeTime())
                    || schedule.openTime().equals(schedule.closeTime())) return false;
        }
        return hasOpenDay;
    }

    private boolean isValidMarketplaceTime(String value) {
        if (value == null || !value.matches("(?:[01]\\d|2[0-3]):[0-5]\\d")) return false;
        try {
            LocalTime.parse(value);
            return true;
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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
                                        .orElse(null)),
                        store.getOpeningHours()))
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
                store.getCreatedAt(), store.getUpdatedAt(), store.getOpeningHours());
    }

    private com.x.store.dto.StoreOpeningHours validateOpeningHours(com.x.store.dto.StoreOpeningHours hours) {
        if (hours == null) {
            return null;
        }
        if (hours.timeZone() == null || hours.timeZone().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Opening hours time zone is required");
        }
        try {
            ZoneId.of(hours.timeZone());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Opening hours time zone is invalid");
        }
        if (hours.days() == null || !hours.days().keySet().equals(EnumSet.allOf(DayOfWeek.class))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Opening hours must include all seven days");
        }
        for (DayOfWeek day : DayOfWeek.values()) {
            var schedule = hours.days().get(day);
            if (schedule == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Opening hours schedule is missing for " + day);
            }
            if (schedule.closed()) {
                continue;
            }
            validateTime(schedule.openTime(), day);
            validateTime(schedule.closeTime(), day);
            if (schedule.openTime().equals(schedule.closeTime())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Opening and closing times must differ for " + day);
            }
        }
        return hours;
    }

    private void validateTime(String time, DayOfWeek day) {
        if (time == null || !time.matches("(?:[01]\\d|2[0-3]):[0-5]\\d")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use a valid HH:mm time for " + day);
        }
        try {
            LocalTime.parse(time);
        } catch (DateTimeParseException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use a valid HH:mm time for " + day);
        }
    }
}
