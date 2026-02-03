package com.example.userservice.repositories.followUserRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.userservice.entities.FollowUser;

import java.util.UUID;

@Repository
public interface FollowUserCommandRepository extends JpaRepository<FollowUser, UUID> {

    @Modifying
    @Transactional
    @Query("INSERT INTO FollowUser (id, followerId, followingId) VALUES (:id, :followerId, :followingId)")
    void followUser(@Param("id") UUID id, @Param("followerId") UUID followerId, @Param("followingId") UUID followingId);

}
