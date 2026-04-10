package com.pinlio.pinservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresignUploadResponse {
    private String imageKey;
    private String uploadUrl;
    private Instant expiresAt;
}