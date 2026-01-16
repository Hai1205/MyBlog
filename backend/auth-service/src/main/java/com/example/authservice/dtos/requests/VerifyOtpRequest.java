package com.example.authservice.dtos.requests;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyOtpRequest {
    private String otp;
    private Boolean isActivation;
}