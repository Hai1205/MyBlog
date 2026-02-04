package com.example.notiservice.repositories;

import com.example.notiservice.entities.Noti;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SimpleNotiRepository extends MongoRepository<Noti, UUID> {
    // Simple repository for basic CRUD operations
}
