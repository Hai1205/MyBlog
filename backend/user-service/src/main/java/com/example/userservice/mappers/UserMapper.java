package com.example.userservice.mappers;

import com.example.userservice.dtos.UserDto;
import com.example.userservice.entities.User;
import com.example.userservice.entities.User.UserRole;
import com.example.userservice.entities.User.UserStatus;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    /**
     * Maps User entity to UserDto
     */
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullname(user.getFullname());
        dto.setBirth(user.getBirth());
        dto.setSummary(user.getSummary());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setStatus(user.getStatus() != null ? user.getStatus().name() : null);
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setFacebook(user.getFacebook());
        dto.setLinkedin(user.getLinkedin());
        dto.setInstagram(user.getInstagram());
        return dto;
    }

    /**
     * Maps UserDto to User entity
     */
    public User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId()); // Set ID for updates
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setEmail(dto.getEmail());
        user.setFullname(dto.getFullname());
        user.setBirth(dto.getBirth());
        user.setSummary(dto.getSummary());

        if (dto.getRole() != null) {
            try {
                user.setRole(UserRole.valueOf(dto.getRole()));
            } catch (IllegalArgumentException e) {
                user.setRole(UserRole.user);
            }
        }

        if (dto.getStatus() != null) {
            try {
                user.setStatus(UserStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                user.setStatus(UserStatus.pending);
            }
        }

        user.setAvatarUrl(dto.getAvatarUrl());
        user.setAvatarPublicId(dto.getAvatarPublicId());
        user.setFacebook(dto.getFacebook());
        user.setLinkedin(dto.getLinkedin());
        user.setInstagram(dto.getInstagram());

        return user;
    }
}
