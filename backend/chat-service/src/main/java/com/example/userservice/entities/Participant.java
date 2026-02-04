package com.example.chatservice.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "participants")
@Data
@NoArgsConstructor // Empty constructor for MapStruct
@EntityListeners(AuditingEntityListener.class)
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String username;
    private String avatarUrl;
    private Boolean isOnline;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
