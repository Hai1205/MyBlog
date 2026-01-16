package com.example.blogservice.repositories.blogRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.blogservice.entities.Blog;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface BlogCommandRepository extends JpaRepository<Blog, UUID> {

       @Modifying
       @Transactional
       @Query("UPDATE Blog b SET b.title = :title, b.updatedAt = :updatedAt WHERE b.id = :blogId")
       int updateBlogTitle(@Param("blogId") UUID blogId,
                     @Param("title") String title,
                     @Param("updatedAt") Instant updatedAt);

       @Modifying
       @Transactional
       @Query(value = "INSERT INTO cvs (id, user_id, title, skills, is_visibility, color, template, font, created_at, updated_at) "
                     +
                     "VALUES (:id, :userId, :title, :skills, :isVisibility, :color, :template, :font, :createdAt, :updatedAt)", nativeQuery = true)
       int insertCV(@Param("id") UUID id,
                     @Param("userId") UUID userId,
                     @Param("title") String title,
                     @Param("skills") String skills,
                     @Param("isVisibility") boolean isVisibility,
                     @Param("color") String color,
                     @Param("template") String template,
                     @Param("font") String font,
                     @Param("createdAt") Instant createdAt,
                     @Param("updatedAt") Instant updatedAt);

       @Modifying
       @Transactional
       @Query("DELETE FROM Blog b WHERE b.id = :blogId")
       int deleteBlogById(@Param("blogId") UUID blogId);

       @Modifying
       @Transactional
       @Query("DELETE FROM Blog b WHERE b.authorId = :userId")
       int deleteAllBlogsByUserId(@Param("userId") UUID userId);

       @Modifying
       @Transactional
       @Query(value = "INSERT INTO blogs (id, author_id, title, description, category, image_url, image_public_id, content, created_at, updated_at) "
                     +
                     "VALUES (:id, :authorId, :title, :description, :category, :imageUrl, :imagePublicId, :content, :createdAt, :updatedAt)", nativeQuery = true)
       int insertBlog(@Param("id") UUID id,
                     @Param("authorId") UUID authorId,
                     @Param("title") String title,
                     @Param("description") String description,
                     @Param("category") String category,
                     @Param("imageUrl") String imageUrl,
                     @Param("imagePublicId") String imagePublicId,
                     @Param("content") String content,
                     @Param("createdAt") Instant createdAt,
                     @Param("updatedAt") Instant updatedAt);

       @Modifying
       @Transactional
       @Query("UPDATE Blog b SET b.title = :title, b.description = :description, b.category = :category, " +
                     "b.content = :content, b.imageUrl = :imageUrl, b.imagePublicId = :imagePublicId, b.updatedAt = :updatedAt "
                     +
                     "WHERE b.id = :blogId")
       int updateBlog(@Param("blogId") UUID blogId,
                     @Param("title") String title,
                     @Param("description") String description,
                     @Param("category") String category,
                     @Param("content") String content,
                     @Param("imageUrl") String imageUrl,
                     @Param("imagePublicId") String imagePublicId,
                     @Param("updatedAt") Instant updatedAt);
}
