package com.example.blogservice.repositories.blogRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.blogservice.entities.Blog;

import java.util.UUID;

@Repository
public interface SimpleBlogRepository extends JpaRepository<Blog, UUID> {
    // - Optional<Blog> findById(UUID id)
    // - boolean existsById(UUID id)
    // - Blog save(Blog entity)
    // - void deleteById(UUID id)
    // - List<Blog> findAll()
}
