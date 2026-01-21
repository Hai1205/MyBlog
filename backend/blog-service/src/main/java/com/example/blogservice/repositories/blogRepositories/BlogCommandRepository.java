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
       @Query("DELETE FROM Blog b WHERE b.id = :blogId")
       int deleteBlogById(@Param("blogId") UUID blogId);

       @Modifying
       @Transactional
       @Query("DELETE FROM Blog b WHERE b.authorId = :userId")
       int deleteAllBlogsByUserId(@Param("userId") UUID userId);

       @Modifying
       @Transactional
       @Query(value = "INSERT INTO blogs (id, author_id, title, description, category, thumbnail_url, thumbnail_public_id, content, is_visibility, created_at, updated_at) "
                     +
                     "VALUES (:id, :authorId, :title, :description, :category, :thumbnailUrl, :thumbnailPublicId, :content, :isVisibility, :createdAt, :updatedAt)", nativeQuery = true)
       int insertBlog(@Param("id") UUID id,
                     @Param("authorId") UUID authorId,
                     @Param("title") String title,
                     @Param("description") String description,
                     @Param("category") String category,
                     @Param("thumbnailUrl") String thumbnailUrl,
                     @Param("thumbnailPublicId") String thumbnailPublicId,
                     @Param("content") String content,
                     @Param("isVisibility") Boolean isVisibility,
                     @Param("createdAt") Instant createdAt,
                     @Param("updatedAt") Instant updatedAt);

       @Modifying
       @Transactional
       @Query("UPDATE Blog b SET b.title = :title, b.description = :description, b.category = :category, " +
                     "b.content = :content, b.thumbnailUrl = :thumbnailUrl, b.thumbnailPublicId = :thumbnailPublicId, b.isVisibility = :isVisibility, b.updatedAt = :updatedAt "
                     +
                     "WHERE b.id = :blogId")
       int updateBlog(@Param("blogId") UUID blogId,
                     @Param("title") String title,
                     @Param("description") String description,
                     @Param("category") Blog.Category category,
                     @Param("content") String content,
                     @Param("thumbnailUrl") String thumbnailUrl,
                     @Param("thumbnailPublicId") String thumbnailPublicId,
                     @Param("isVisibility") Boolean isVisibility,
                     @Param("updatedAt") Instant updatedAt);
}
