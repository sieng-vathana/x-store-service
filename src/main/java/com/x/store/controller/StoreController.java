package com.x.store.controller;

import com.x.store.dto.CreateStoreRequest;
import com.x.store.dto.StoreResponse;
import com.x.store.service.StoreService;
import com.sharedlib.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<ApiResponse<StoreResponse>> create(@Valid @RequestBody CreateStoreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Store created", storeService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StoreResponse>> getById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), storeService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getByBusiness(@RequestParam @Positive Long businessId) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), storeService.getByBusiness(businessId)));
    }
}
