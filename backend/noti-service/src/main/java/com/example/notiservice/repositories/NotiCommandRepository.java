package com.example.notiservice.repositories;

import com.example.notiservice.entities.Noti;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotiCommandRepository extends MongoRepository<Noti, UUID> {

    @Query(value = "{ '_id': ?0 }", delete = true)
    long deleteNotiById(UUID notiId);

    @Query(value = "{ 'receiverId': ?0 }", delete = true)
    long deleteByReceiverId(UUID receiverId);

    @Query(value = "{ 'authorId': ?0 }", delete = true)
    long deleteByAuthorId(UUID authorId);
}
