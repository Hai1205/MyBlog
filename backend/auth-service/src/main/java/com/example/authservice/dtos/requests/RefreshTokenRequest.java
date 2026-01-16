package com.example.authservice.dtos.requests;

import lombok.Data;

/**
 * DTO cho Refresh Token Request
 * Nhận refresh token từ client để tạo access token mới
 */
@Data
public class RefreshTokenRequest {

    /**
     * Refresh Token được gửi từ client
     * Token này phải hợp lệ và chưa hết hạn
     */
    private String refreshToken;
}
