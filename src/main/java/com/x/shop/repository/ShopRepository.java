package com.x.shop.repository;

import com.x.shop.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    boolean existsByBusinessIdAndCode(Long businessId, String code);

    List<Shop> findAllByBusinessIdOrderByCreatedAtDesc(Long businessId);
}
