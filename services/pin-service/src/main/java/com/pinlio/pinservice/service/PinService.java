package com.pinlio.pinservice.service;

import com.pinlio.pinservice.dto.PinCreateRequest;
import com.pinlio.pinservice.dto.PinResponse;
import com.pinlio.pinservice.dto.PinUpdateRequest;
import com.pinlio.pinservice.dto.PresignUploadRequest;
import com.pinlio.pinservice.dto.PresignUploadResponse;
import com.pinlio.pinservice.entity.Pin;
import com.pinlio.pinservice.entity.PinVisibility;
import com.pinlio.pinservice.repository.PinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PinService {

    private final PinRepository pinRepository;
    private final S3ImageService s3ImageService;

    public PresignUploadResponse createPresignedUpload(UUID requesterId, PresignUploadRequest request) {
        return s3ImageService.createPresignedUpload(requesterId, request);
    }

    public PinResponse createPin(UUID requesterId, PinCreateRequest request) {
        Pin pin = Pin.builder()
                .userId(requesterId)
                .boardId(request.getBoardId())
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .imageKey(request.getImageKey().trim())
                .sourceUrl(request.getSourceUrl())
                .tags(toTagArray(request.getTags()))
                .visibility(request.getVisibility() == null ? PinVisibility.PUBLIC : request.getVisibility())
                .build();

        Pin savedPin = pinRepository.save(pin);
        return toPinResponse(savedPin);
    }

    @Transactional(readOnly = true)
    public PinResponse getPinById(UUID pinId) {
        Pin pin = pinRepository.findByIdAndIsActiveTrue(pinId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pin not found"));
        return toPinResponse(pin);
    }

    @Transactional(readOnly = true)
    public List<PinResponse> getPinsByUserId(UUID userId) {
        return pinRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toPinResponse)
                .toList();
    }

    public PinResponse updatePin(UUID requesterId, UUID pinId, PinUpdateRequest request) {
        Pin pin = pinRepository.findByIdAndIsActiveTrue(pinId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pin not found"));

        if (!pin.getUserId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot update another user's pin");
        }

        if (request.getTitle() != null) {
            pin.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            pin.setDescription(request.getDescription());
        }
        if (request.getImageKey() != null) {
            pin.setImageKey(request.getImageKey().trim());
        }
        if (request.getSourceUrl() != null) {
            pin.setSourceUrl(request.getSourceUrl());
        }
        if (request.getBoardId() != null) {
            pin.setBoardId(request.getBoardId());
        }
        if (request.getTags() != null) {
            pin.setTags(toTagArray(request.getTags()));
        }
        if (request.getVisibility() != null) {
            pin.setVisibility(request.getVisibility());
        }

        Pin updatedPin = pinRepository.save(pin);
        return toPinResponse(updatedPin);
    }

    public void deletePin(UUID requesterId, UUID pinId) {
        Pin pin = pinRepository.findByIdAndIsActiveTrue(pinId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pin not found"));

        if (!pin.getUserId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete another user's pin");
        }

        pin.setIsActive(false);
        pinRepository.save(pin);
    }

    private PinResponse toPinResponse(Pin pin) {
        return PinResponse.builder()
                .id(pin.getId())
                .userId(pin.getUserId())
                .boardId(pin.getBoardId())
                .title(pin.getTitle())
                .description(pin.getDescription())
                .imageKey(pin.getImageKey())
                .imageUrl(s3ImageService.generateDownloadUrl(pin.getImageKey()))
                .sourceUrl(pin.getSourceUrl())
                .tags(toTagList(pin.getTags()))
                .visibility(pin.getVisibility())
                .createdAt(pin.getCreatedAt())
                .updatedAt(pin.getUpdatedAt())
                .build();
    }

    private String[] toTagArray(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new String[0];
        }

        return tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .distinct()
                .toArray(String[]::new);
    }

    private List<String> toTagList(String[] tags) {
        if (tags == null || tags.length == 0) {
            return List.of();
        }
        return Arrays.stream(tags).toList();
    }
}