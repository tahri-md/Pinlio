package com.pinlio.userservice.repository;

import com.pinlio.userservice.entity.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {
    Optional<UserFollow> findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);
    List<UserFollow> findByFollowerId(UUID followerId);
    List<UserFollow> findByFollowingId(UUID followingId);
    long countByFollowerId(UUID followerId);
    long countByFollowingId(UUID followingId);
}
