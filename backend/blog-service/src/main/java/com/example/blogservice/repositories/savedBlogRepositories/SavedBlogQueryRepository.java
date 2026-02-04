package com.example.blogservice.repositories.savedBlogRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.blogservice.entities.SavedBlog;
import com.example.blogservice.entities.Blog;

import java.util.List;
import java.util.UUID;

@Repository
public interface SavedBlogQueryRepository extends JpaRepository<SavedBlog, UUID> {

    @Query("SELECT sb FROM SavedBlog sb WHERE sb.userId = :userId")
    List<SavedBlog> findSavedBlogsByUserId(@Param("userId") UUID userId);

    @Query("SELECT sb FROM SavedBlog sb WHERE sb.blogId = :blogId")
    List<SavedBlog> findSavedBlogsByBlogId(@Param("blogId") UUID blogId);

    @Query("SELECT b FROM Blog b JOIN SavedBlog sb ON b.id = sb.blogId WHERE sb.userId = :userId ORDER BY sb.id DESC")
    List<Blog> findBlogsByUserSaved(@Param("userId") UUID userId);

}
