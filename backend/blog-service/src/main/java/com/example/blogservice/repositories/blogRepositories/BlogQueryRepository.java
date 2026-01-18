package com.example.blogservice.repositories.blogRepositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.blogservice.entities.Blog;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlogQueryRepository extends JpaRepository<Blog, UUID> {

        @Query("SELECT b FROM Blog b WHERE b.id = :id")
        Optional<Blog> findBlogById(@Param("id") UUID id);

        @Query("SELECT b FROM Blog b WHERE b.authorId = :userId ORDER BY b.createdAt DESC")
        Page<Blog> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);

        @Query("SELECT b FROM Blog b WHERE b.authorId = :userId ORDER BY b.createdAt DESC")
        List<Blog> findBlogsByUserId(@Param("userId") UUID userId);

        @Query("SELECT b FROM Blog b ORDER BY b.createdAt DESC")
        Page<Blog> findAllBlogs(Pageable pageable);

        @Query("SELECT b FROM Blog b ORDER BY b.createdAt DESC")
        List<Blog> findAllBlogs();

        @Query("SELECT COUNT(b) FROM Blog b")
        long countTotalBlogs();

        @Query("SELECT b FROM Blog b WHERE b.createdAt BETWEEN :startDate AND :endDate ORDER BY b.createdAt DESC")
        Page<Blog> findBlogsCreatedBetween(@Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate,
                        Pageable pageable);

        @Query("SELECT b FROM Blog b WHERE b.createdAt BETWEEN :startDate AND :endDate ORDER BY b.createdAt DESC")
        List<Blog> findBlogsCreatedBetween(@Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        @Query("SELECT COUNT(b) FROM Blog b WHERE b.createdAt BETWEEN :startDate AND :endDate")
        long countBlogsCreatedBetween(@Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        @Query("SELECT b FROM Blog b ORDER BY b.createdAt DESC")
        Page<Blog> findRecentBlogs(Pageable pageable);

        @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Blog b WHERE b.authorId = :userId")
        boolean existsByUserId(@Param("userId") UUID userId);

        @Query("SELECT COUNT(b) FROM Blog b WHERE b.authorId = :userId")
        long countByUserId(@Param("userId") UUID userId);

        // @Query("SELECT b FROM Blog b WHERE b.isVisibility = :isVisibility ORDER BY
        // b.createdAt DESC")
        // Page<Blog> findByVisibility(@Param("isVisibility") Boolean isVisibility,
        // Pageable pageable);

        // @Query("SELECT b FROM Blog b WHERE b.isVisibility = :isVisibility ORDER BY
        // b.createdAt DESC")
        // List<Blog> findByVisibility(@Param("isVisibility") Boolean isVisibility);

        @Query("SELECT COUNT(b) FROM Blog b WHERE b.isVisibility = :isVisibility")
        long countByVisibility(@Param("isVisibility") Boolean isVisibility);
}
