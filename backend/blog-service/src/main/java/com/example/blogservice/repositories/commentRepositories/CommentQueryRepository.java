package com.example.blogservice.repositories.commentRepositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.blogservice.entities.Comment;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentQueryRepository extends JpaRepository<Comment, UUID> {

        @Query("SELECT c FROM Comment c WHERE c.id = :commentId")
        Optional<Comment> findCommentById(@Param("commentId") UUID commentId);

        @Query("SELECT c FROM Comment c WHERE c.blogId = :blogId ORDER BY c.createdAt DESC")
        List<Comment> findCommentsByBlogId(@Param("blogId") UUID blogId);

        @Query("SELECT c FROM Comment c ORDER BY c.createdAt DESC")
        List<Comment> findAllComments();

        @Query("SELECT COUNT(c) FROM Comment c")
        long countTotalComments();

        @Query("SELECT c FROM Comment c WHERE c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
        List<Comment> findCommentsCreatedBetween(@Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        @Query("SELECT COUNT(c) FROM Comment c WHERE c.createdAt BETWEEN :startDate AND :endDate")
        long countCommentsCreatedBetween(@Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        @Query("SELECT c FROM Comment c ORDER BY c.createdAt DESC")
        List<Comment> findRecentComments(Pageable pageable);

        @Query("SELECT COUNT(c) FROM Comment c WHERE c.blogId = :blogId")
        long countByBlogId(@Param("blogId") UUID blogId);

        @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Comment c WHERE c.blogId = :blogId")
        boolean existsByBlogId(@Param("blogId") UUID blogId);
}
