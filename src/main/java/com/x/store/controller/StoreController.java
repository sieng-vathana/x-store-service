package com.x.store.controller;

import com.x.store.dto.CreateStoreRequest;
import com.x.store.dto.UpdateStoreRequest;
import com.x.store.dto.StoreResponse;
import com.x.store.service.StoreService;
import com.sharedlib.response.ApiResponse;
import com.sharedlib.response.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Validated
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<ApiResponse<StoreResponse>> create(@Valid @RequestBody CreateStoreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Store created", storeService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StoreResponse>> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateStoreRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Store updated", storeService.update(id, request)));
    }

    /** Used only to compensate a failed cross-service workspace registration. */
    @DeleteMapping("/{id}/registration-failure")
    public ResponseEntity<Void> deleteFailedRegistration(@PathVariable @Positive Long id) {
        storeService.deleteFailedRegistration(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StoreResponse>> getById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), storeService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StoreResponse>>> getByBusiness(
            @RequestParam @Positive Long businessId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), storeService.getByBusiness(businessId, page, size)));
    }
}
