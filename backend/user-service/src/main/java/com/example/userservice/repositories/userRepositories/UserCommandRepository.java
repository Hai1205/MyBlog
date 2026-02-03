package com.example.userservice.repositories.userRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.userservice.entities.User;
import com.example.userservice.entities.User.UserRole;
import com.example.userservice.entities.User.UserStatus;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface UserCommandRepository extends JpaRepository<User, UUID> {

        @Modifying
        @Transactional
        @Query("UPDATE User u SET u.status = :status, u.updatedAt = :updatedAt WHERE u.id = :userId")
        int updateUserStatus(@Param("userId") UUID userId,
                        @Param("status") UserStatus status,
                        @Param("updatedAt") Instant updatedAt);

        @Modifying
        @Transactional
        @Query("UPDATE User u SET u.status = :status, u.updatedAt = :updatedAt WHERE u.email = :email")
        int updateUserStatusByEmail(@Param("email") String email,
                        @Param("status") UserStatus status,
                        @Param("updatedAt") Instant updatedAt);

        @Modifying
        @Transactional
        @Query("UPDATE User u SET u.password = :password, u.updatedAt = :updatedAt WHERE u.id = :userId")
        int updateUserPassword(@Param("userId") UUID userId,
                        @Param("password") String password,
                        @Param("updatedAt") Instant updatedAt);

        @Modifying
        @Transactional
        @Query("UPDATE User u SET u.password = :password, u.updatedAt = :updatedAt WHERE u.email = :email")
        int updateUserPasswordByEmail(@Param("email") String email,
                        @Param("password") String password,
                        @Param("updatedAt") Instant updatedAt);

        @Modifying
        @Transactional
        @Query("UPDATE User u SET " +
                        "u.birth = :birth, u.summary = :summary, u.facebook = :facebook, u.linkedin = :linkedin, u.instagram = :instagram, u.updatedAt = :updatedAt WHERE u.id = :userId")
        int updateUserBasicInfo(@Param("userId") UUID userId,
                        @Param("birth") String birth,
                        @Param("summary") String summary,
                        @Param("facebook") String facebook,
                        @Param("linkedin") String linkedin,
                        @Param("instagram") String instagram,
                        @Param("updatedAt") Instant updatedAt);

        @Modifying
        @Transactional
        @Query("UPDATE User u SET u.role = :role, u.updatedAt = :updatedAt WHERE u.id = :userId")
        int updateUserRole(@Param("userId") UUID userId,
                        @Param("role") UserRole role,
                        @Param("updatedAt") Instant updatedAt);

        @Modifying
        @Transactional
        @Query("UPDATE User u SET u.avatarUrl = :avatarUrl, u.avatarPublicId = :avatarPublicId, u.updatedAt = :updatedAt WHERE u.id = :userId")
        int updateUserAvatar(@Param("userId") UUID userId,
                        @Param("avatarUrl") String avatarUrl,
                        @Param("avatarPublicId") String avatarPublicId,
                        @Param("updatedAt") Instant updatedAt);

        @Modifying
        @Transactional
        @Query("DELETE FROM User u WHERE u.id = :userId")
        int deleteUserById(@Param("userId") UUID userId);

        @Modifying
        @Transactional
        @Query("DELETE FROM User u WHERE u.email = :email")
        int deleteUserByEmail(@Param("email") String email);

        @Modifying
        @Transactional
        @Query("UPDATE User u SET u.birth = :birth, u.updatedAt = :updatedAt WHERE u.id = :userId")
        int updateUserBirth(@Param("userId") UUID userId,
                        @Param("birth") String birth,
                        @Param("updatedAt") Instant updatedAt);

        @Modifying
        @Transactional
        @Query("UPDATE User u SET u.summary = :summary, u.updatedAt = :updatedAt WHERE u.id = :userId")
        int updateUserSummary(@Param("userId") UUID userId,
                        @Param("summary") String summary,
                        @Param("updatedAt") Instant updatedAt);

        @Modifying
        @Transactional
        @Query("UPDATE User u SET u.facebook = :facebook, u.updatedAt = :updatedAt WHERE u.id = :userId")
        int updateUserFacebook(@Param("userId") UUID userId,
                        @Param("facebook") String facebook,
                        @Param("updatedAt") Instant updatedAt);

        @Modifying
        @Transactional
        @Query("UPDATE User u SET u.instagram = :instagram, u.updatedAt = :updatedAt WHERE u.id = :userId")
        int updateUserInstagram(@Param("userId") UUID userId,
                        @Param("instagram") String instagram,
                        @Param("updatedAt") Instant updatedAt);

        @Modifying
        @Transactional
        @Query("UPDATE User u SET u.linkedin = :linkedin, u.updatedAt = :updatedAt WHERE u.id = :userId")
        int updateUserLinkedin(@Param("userId") UUID userId,
                        @Param("linkedin") String linkedin,
                        @Param("updatedAt") Instant updatedAt);

        @Modifying
        @Transactional
        @Query(value = "INSERT INTO users (id, username, email, password, birth, summary, " +
                        "avatar_url, avatar_public_id, role, status, facebook, linkedin, instagram, created_at, updated_at) " +
                        "VALUES (:id, :username, :email, :password, :birth, :summary, " +
                        ":avatarUrl, :avatarPublicId, :role, :status, :facebook, :linkedin, :instagram, :createdAt, :updatedAt)", nativeQuery = true)
        int insertUser(@Param("id") UUID id,
                        @Param("username") String username,
                        @Param("email") String email,
                        @Param("password") String password,
                        @Param("birth") String birth,
                        @Param("summary") String summary,
                        @Param("avatarUrl") String avatarUrl,
                        @Param("avatarPublicId") String avatarPublicId,
                        @Param("role") String role,
                        @Param("status") String status,
                        @Param("facebook") String facebook,
                        @Param("linkedin") String linkedin,
                        @Param("instagram") String instagram,
                        @Param("createdAt") Instant createdAt,
                        @Param("updatedAt") Instant updatedAt);

        @Modifying
        @Transactional
        @Query("UPDATE User u SET u.birth = :birth, u.summary = :summary," +
                        "u.avatarUrl = :avatarUrl, u.avatarPublicId = :avatarPublicId, "
                        +
                        "u.role = :role, u.status = :status, u.facebook = :facebook, u.linkedin = :linkedin, u.instagram = :instagram,"
                        + "u.updatedAt = :updatedAt WHERE u.id = :userId")
        int updateUserAllFields(@Param("userId") UUID userId,
                        @Param("birth") String birth,
                        @Param("summary") String summary,
                        @Param("avatarUrl") String avatarUrl,
                        @Param("avatarPublicId") String avatarPublicId,
                        @Param("role") UserRole role,
                        @Param("status") UserStatus status,
                        @Param("facebook") String facebook,
                        @Param("linkedin") String linkedin,
                        @Param("instagram") String instagram,
                        @Param("updatedAt") Instant updatedAt);
}
