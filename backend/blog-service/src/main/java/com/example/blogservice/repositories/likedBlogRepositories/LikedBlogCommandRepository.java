package com.example.blogservice.repositories.likedBlogRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.blogservice.entities.LikedBlog;

import java.util.UUID;

@Repository
public interface LikedBlogCommandRepository extends JpaRepository<LikedBlog, UUID> {

    @Modifying
    @Transactional
    @Query("INSERT INTO LikedBlog (id, userId, blogId) VALUES (:id, :userId, :blogId)")
    void likeBlog(@Param("id") UUID id, @Param("userId") UUID userId, @Param("blogId") UUID blogId);
}
