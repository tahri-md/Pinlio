package com.pinlio.pinservice.dto;

import com.pinlio.pinservice.entity.PinVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PinResponse {
    private UUID id;
    private UUID userId;
    private UUID boardId;
    private String title;
    private String description;
    private String imageKey;
    private String imageUrl;
    private String sourceUrl;
    private List<String> tags;
    private PinVisibility visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}