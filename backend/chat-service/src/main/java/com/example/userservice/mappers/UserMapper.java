package com.example.chatservice.mappers;

import com.example.userservice.dtos.UserDto;
import com.example.userservice.dtos.responses.views.UserView;
import com.example.userservice.entities.User;

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
        dto.setBirth(user.getBirth());
        dto.setSummary(user.getSummary());

        if (user.getRole() != null) {
            dto.setRole(user.getRole().name());
        } else {
            dto.setRole(User.UserRole.user.name());
        }

        if (user.getStatus() != null) {
            dto.setStatus(user.getStatus().name());
        } else {
            dto.setStatus(User.UserStatus.pending.name());
        }
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setFacebook(user.getFacebook());
        dto.setLinkedin(user.getLinkedin());
        dto.setInstagram(user.getInstagram());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
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
        user.setBirth(dto.getBirth());
        user.setSummary(dto.getSummary());

        if (dto.getRole() != null) {
            try {
                user.setRole(User.UserRole.valueOf(dto.getRole()));
            } catch (IllegalArgumentException e) {
                user.setRole(User.UserRole.user);
            }
        }

        if (dto.getStatus() != null) {
            try {
                user.setStatus(User.UserStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                user.setStatus(User.UserStatus.pending);
            }
        }

        user.setAvatarUrl(dto.getAvatarUrl());
        user.setAvatarPublicId(dto.getAvatarPublicId());
        user.setFacebook(dto.getFacebook());
        user.setLinkedin(dto.getLinkedin());
        user.setInstagram(dto.getInstagram());

        return user;
    }

    /**
     * Maps UserDto to User View
     */
    public UserView dtoToView(UserDto dto) {
        if (dto == null) {
            return null;
        }

        UserView user = new UserView();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());

        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        } else {
            user.setRole(User.UserRole.user.name());
        }

        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        } else {
            user.setStatus(User.UserStatus.pending.name());
        }

        user.setAvatarUrl(dto.getAvatarUrl());
        user.setCreatedAt(dto.getCreatedAt());

        return user;

    }

    /**
     * Maps User Entity to User View
     */
    public UserView entityToView(User user) {
        if (user == null) {
            return null;
        }

        UserView userView = new UserView();
        userView.setId(user.getId());
        userView.setUsername(user.getUsername());
        userView.setEmail(user.getEmail());

        if (user.getRole() != null) {
            userView.setRole(user.getRole().name());
        } else {
            userView.setRole(User.UserRole.user.name());
        }

        if (user.getStatus() != null) {
            userView.setStatus(user.getStatus().name());
        } else {
            userView.setStatus(User.UserStatus.pending.name());
        }

        userView.setAvatarUrl(user.getAvatarUrl());
        userView.setCreatedAt(user.getCreatedAt());

        return userView;
    }
}
