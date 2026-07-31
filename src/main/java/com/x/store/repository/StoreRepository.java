package com.x.store.repository;

import com.x.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface StoreRepository extends JpaRepository<Store, Long> {
    boolean existsByBusinessIdAndCode(Long businessId, String code);

    Page<Store> findAllByBusinessId(Long businessId, Pageable pageable);
}
