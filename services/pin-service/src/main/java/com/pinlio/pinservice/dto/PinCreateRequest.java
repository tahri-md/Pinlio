package com.pinlio.pinservice.dto;

import com.pinlio.pinservice.entity.PinVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PinCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    @Size(max = 3000, message = "Description must be at most 3000 characters")
    private String description;

    @NotBlank(message = "Image key is required")
    @Size(max = 1024, message = "Image key must be at most 1024 characters")
    private String imageKey;

    @Size(max = 512, message = "Source URL must be at most 512 characters")
    private String sourceUrl;

    private UUID boardId;

    @Builder.Default
    @Size(max = 30, message = "You can provide up to 30 tags")
    private List<@NotBlank(message = "Tag cannot be blank") @Size(max = 100, message = "Each tag must be at most 100 characters") String> tags = List.of();

    private PinVisibility visibility;
}