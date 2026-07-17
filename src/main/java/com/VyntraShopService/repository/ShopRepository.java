package com.VyntraShopService.repository;

import com.VyntraShopService.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    boolean existsByBusinessIdAndCode(Long businessId, String code);

    List<Shop> findAllByBusinessIdOrderByCreatedAtDesc(Long businessId);
}
