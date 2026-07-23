package com.x.store.repository;

import com.x.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {
    boolean existsByBusinessIdAndCode(Long businessId, String code);

    List<Store> findAllByBusinessIdOrderByCreatedAtDesc(Long businessId);
}
