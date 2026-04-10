package com.pinlio.pinservice.repository;

import com.pinlio.pinservice.entity.Pin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PinRepository extends JpaRepository<Pin, UUID> {
    Optional<Pin> findByIdAndIsActiveTrue(UUID pinId);
    List<Pin> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(UUID userId);
    List<Pin> findByBoardIdAndIsActiveTrueOrderByCreatedAtDesc(UUID boardId);
    boolean existsByIdAndUserIdAndIsActiveTrue(UUID pinId, UUID userId);
    long countByUserIdAndIsActiveTrue(UUID userId);
}