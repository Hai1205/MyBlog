package com.example.chatservice.repositories.userRepositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.chatservice.entities.User;
import com.example.chatservice.entities.User.UserStatus;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserQueryRepository extends JpaRepository<User, UUID> {

        @Query("SELECT u FROM User u WHERE u.id = :userId")
        Optional<User> findUserById(@Param("userId") UUID userId);

        @Query("SELECT u FROM User u WHERE u.username = :username")
        Optional<User> findByUsername(@Param("username") String username);

        @Query("SELECT u FROM User u WHERE u.email = :email")
        Optional<User> findByEmail(@Param("email") String email);

        @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username")
        boolean existsByUsername(@Param("username") String username);

        @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email")
        boolean existsByEmail(@Param("email") String email);

        @Query("SELECT COUNT(u) FROM User u")
        long countTotalUsers();

        @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
        long countByStatus(@Param("status") UserStatus status);

        @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
        long countUsersCreatedBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
        Page<User> findRecentUsers(Pageable pageable);

        @Query("SELECT u FROM User u")
        Page<User> findAllUsers(Pageable pageable);

        @Query("SELECT u FROM User u WHERE u.status = :status")
        Page<User> findUsersByStatus(@Param("status") UserStatus status, Pageable pageable);
}
