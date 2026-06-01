package com.rebatesync.infrastructure.adapter.persistence.repository;

import com.rebatesync.infrastructure.adapter.persistence.entity.SyncResultEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaSyncResultRepository extends JpaRepository<SyncResultEntity, Long> {

    @Query("SELECT s FROM SyncResultEntity s ORDER BY s.createdAt DESC LIMIT 1")
    Optional<SyncResultEntity> findLatest();

    List<SyncResultEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
