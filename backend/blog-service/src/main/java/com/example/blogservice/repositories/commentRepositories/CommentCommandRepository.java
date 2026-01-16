package com.example.blogservice.repositories.commentRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.blogservice.entities.Comment;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface CommentCommandRepository extends JpaRepository<Comment, UUID> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO comments (id, blog_id, user_id, content, created_at, updated_at) " +
            "VALUES (:id, :blogId, :userId, :content, :createdAt, :updatedAt)", nativeQuery = true)
    int insertComment(@Param("id") UUID id,
            @Param("blogId") UUID blogId,
            @Param("userId") UUID userId,
            @Param("content") String content,
            @Param("createdAt") Instant createdAt,
            @Param("updatedAt") Instant updatedAt);

    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.content = :content, c.updatedAt = :updatedAt WHERE c.id = :commentId")
    int updateComment(@Param("commentId") UUID commentId,
            @Param("content") String content,
            @Param("updatedAt") Instant updatedAt);

    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.blogId = :blogId")
    int deleteAllByBlogId(@Param("blogId") UUID blogId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.id = :commentId")
    int deleteCommentById(@Param("commentId") UUID commentId);
}
