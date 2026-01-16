package com.example.blogservice.repositories.savedBlogRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.blogservice.entities.SavedBlog;

import java.util.UUID;

@Repository
public interface SavedBlogCommandRepository extends JpaRepository<SavedBlog, UUID> {

    @Modifying
    @Transactional
    @Query("INSERT INTO SavedBlog (id, userId, blogId) VALUES (:id, :userId, :blogId)")
    void saveSavedBlog(@Param("id") UUID id, @Param("userId") UUID userId, @Param("blogId") UUID blogId);

}
