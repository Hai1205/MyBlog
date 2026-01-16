package com.example.blogservice.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "saved_blogs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedBlog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    
    private UUID id;
    private UUID userId;
    private UUID blogId;
}
