package com.x.store.repository;

import com.x.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface StoreRepository extends JpaRepository<Store, Long> {
    boolean existsByBusinessIdAndCode(Long businessId, String code);

    Page<Store> findAllByBusinessId(Long businessId, Pageable pageable);

    Page<Store> findAllByMarketplaceStatus(String marketplaceStatus, Pageable pageable);

    @Query("""
            SELECT s FROM Store s
            WHERE s.marketplaceStatus = 'APPROVED'
              AND (s.status IS NULL OR s.status = 1)
              AND s.suspendedAt IS NULL
              AND (s.salesChannel IS NULL OR s.salesChannel IN ('ONLINE', 'BOTH'))
              AND (:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(s.city) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Store> findMarketplaceDirectory(@Param("search") String search, Pageable pageable);
}
