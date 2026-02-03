package com.example.userservice.repositories.followUserRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.userservice.entities.FollowUser;
import com.example.userservice.entities.User;

import java.util.List;
import java.util.UUID;

@Repository
public interface FollowUserQueryRepository extends JpaRepository<FollowUser, UUID> {

    @Query("SELECT fb FROM FollowUser fb WHERE fb.followerId = :userId")
    List<FollowUser> findFollowUsersByUserId(@Param("userId") UUID userId);

    @Query("SELECT b FROM User b JOIN FollowUser fb ON b.id = fb.followingId WHERE fb.followerId = :userId ORDER BY fb.id DESC")
    List<User> findUsersByUserFollowed(@Param("userId") UUID userId);

    @Query("SELECT u FROM User u JOIN FollowUser fu ON u.id = fu.followerId WHERE fu.followingId = :userId ORDER BY fu.id DESC")
    List<User> findFollowersByUserId(@Param("userId") UUID userId);

}
