package com.x.shop.controller;

import com.x.shop.dto.CreateShopRequest;
import com.x.shop.dto.ShopResponse;
import com.x.shop.service.ShopService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShopResponse create(@Valid @RequestBody CreateShopRequest request) {
        return shopService.create(request);
    }

    @GetMapping("/{id}")
    public ShopResponse getById(@PathVariable @Positive Long id) {
        return shopService.getById(id);
    }

    @GetMapping
    public List<ShopResponse> getByBusiness(@RequestParam @Positive Long businessId) {
        return shopService.getByBusiness(businessId);
    }
}
