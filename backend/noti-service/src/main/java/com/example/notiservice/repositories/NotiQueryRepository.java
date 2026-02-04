package com.example.notiservice.repositories;

import com.example.notiservice.entities.Noti;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotiQueryRepository extends MongoRepository<Noti, UUID> {

    @Query("{ '_id': ?0 }")
    Optional<Noti> findNotiById(UUID notiId);

    @Query("{ 'receiverId': ?0 }")
    List<Noti> findByReceiverId(UUID receiverId);

    @Query("{ 'receiverId': ?0 }")
    Page<Noti> findByReceiverId(UUID receiverId, Pageable pageable);

    @Query("{ 'receiverId': ?0, 'isRead': false }")
    List<Noti> findUnreadByReceiverId(UUID receiverId);

    @Query("{ 'receiverId': ?0, 'isRead': false }")
    long countUnreadByReceiverId(UUID receiverId);

    @Query(value = "{}", sort = "{ 'createdAt': -1 }")
    Page<Noti> findAllNotis(Pageable pageable);

    @Query("{ 'authorId': ?0 }")
    List<Noti> findByAuthorId(UUID authorId);

    @Query(value = "{ 'receiverId': ?0 }", sort = "{ 'createdAt': -1 }")
    List<Noti> findByReceiverIdOrderByCreatedAtDesc(UUID receiverId);
}
