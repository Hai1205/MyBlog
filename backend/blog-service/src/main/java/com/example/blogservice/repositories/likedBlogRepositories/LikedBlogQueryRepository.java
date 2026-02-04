package com.example.blogservice.repositories.likedBlogRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.blogservice.entities.Blog;
import com.example.blogservice.entities.LikedBlog;

import java.util.List;
import java.util.UUID;

@Repository
public interface LikedBlogQueryRepository extends JpaRepository<LikedBlog, UUID> {

    @Query("SELECT lb FROM LikedBlog lb WHERE lb.userId = :userId")
    List<LikedBlog> findLikedBlogsByUserId(@Param("userId") UUID userId);

    @Query("SELECT lb FROM LikedBlog lb WHERE lb.blogId = :blogId")
    List<LikedBlog> findLikedBlogsByBlogId(@Param("blogId") UUID blogId);

    @Query("SELECT b FROM Blog b JOIN LikedBlog lb ON b.id = lb.blogId WHERE lb.userId = :userId ORDER BY lb.id DESC")
    List<Blog> findLikedBlogsDetailsByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(lb) FROM LikedBlog lb WHERE lb.blogId = :blogId")
    long countLikesByBlogId(@Param("blogId") UUID blogId);
}
