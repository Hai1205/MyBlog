package com.example.userservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.userservice.entities.User;

import java.util.UUID;

/**
 * SimpleUserRepository - Chỉ sử dụng các JPA methods cơ bản
 * - existsById() - kiểm tra tồn tại theo ID
 * - findById() - tìm entity theo ID
 */
@Repository
public interface SimpleUserRepository extends JpaRepository<User, UUID> {
    // - Optional<User> findById(UUID id)
    // - boolean existsById(UUID id)
    // - User save(User entity)
    // - void deleteById(UUID id)
    // - List<User> findAll()
}
