package com.example.blogservice.repositories.commentRepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.blogservice.entities.Comment;

import java.util.UUID;


@Repository
public interface SimpleCommentRepository extends JpaRepository<Comment, UUID> {}
