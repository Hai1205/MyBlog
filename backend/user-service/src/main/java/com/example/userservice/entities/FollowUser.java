package com.example.userservice.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "follow_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID followerId;
    private UUID followingId;
}
