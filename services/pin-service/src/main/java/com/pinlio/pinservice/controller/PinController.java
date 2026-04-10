package com.pinlio.pinservice.controller;

import com.pinlio.pinservice.dto.ApiResponse;
import com.pinlio.pinservice.dto.PinCreateRequest;
import com.pinlio.pinservice.dto.PinResponse;
import com.pinlio.pinservice.dto.PinUpdateRequest;
import com.pinlio.pinservice.dto.PresignUploadRequest;
import com.pinlio.pinservice.dto.PresignUploadResponse;
import com.pinlio.pinservice.service.PinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pins")
@RequiredArgsConstructor
public class PinController {

    public static final String REQUEST_USER_ID_ATTR = "X-User-Id";

    private final PinService pinService;

    @PostMapping("/uploads/presign")
    public ResponseEntity<ApiResponse<PresignUploadResponse>> createPresignedUpload(
            @RequestAttribute(value = REQUEST_USER_ID_ATTR, required = false) UUID requesterIdFromAttr,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterIdFromHeader,
            @Valid @RequestBody PresignUploadRequest request) {

        UUID requesterId = resolveRequesterId(requesterIdFromAttr, requesterIdFromHeader);
        PresignUploadResponse upload = pinService.createPresignedUpload(requesterId, request);

        return ResponseEntity.ok(ApiResponse.success(upload, "Presigned upload URL created"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PinResponse>> createPin(
            @RequestAttribute(value = REQUEST_USER_ID_ATTR, required = false) UUID requesterIdFromAttr,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterIdFromHeader,
            @Valid @RequestBody PinCreateRequest request) {

        UUID requesterId = resolveRequesterId(requesterIdFromAttr, requesterIdFromHeader);
        PinResponse createdPin = pinService.createPin(requesterId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdPin, "Pin created successfully"));
    }

    @GetMapping("/{pinId}")
    public ResponseEntity<ApiResponse<PinResponse>> getPinById(@PathVariable UUID pinId) {
        PinResponse pin = pinService.getPinById(pinId);
        return ResponseEntity.ok(ApiResponse.success(pin, "Pin retrieved successfully"));
    }

    @PutMapping("/{pinId}")
    public ResponseEntity<ApiResponse<PinResponse>> updatePin(
            @PathVariable UUID pinId,
            @RequestAttribute(value = REQUEST_USER_ID_ATTR, required = false) UUID requesterIdFromAttr,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterIdFromHeader,
            @Valid @RequestBody PinUpdateRequest request) {

        UUID requesterId = resolveRequesterId(requesterIdFromAttr, requesterIdFromHeader);
        PinResponse updatedPin = pinService.updatePin(requesterId, pinId, request);
        return ResponseEntity.ok(ApiResponse.success(updatedPin, "Pin updated successfully"));
    }

    @DeleteMapping("/{pinId}")
    public ResponseEntity<ApiResponse<Void>> deletePin(
            @PathVariable UUID pinId,
            @RequestAttribute(value = REQUEST_USER_ID_ATTR, required = false) UUID requesterIdFromAttr,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterIdFromHeader) {

        UUID requesterId = resolveRequesterId(requesterIdFromAttr, requesterIdFromHeader);
        pinService.deletePin(requesterId, pinId);
        return ResponseEntity.ok(ApiResponse.success(null, "Pin deleted successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<PinResponse>>> getPinsByUserId(@PathVariable UUID userId) {
        List<PinResponse> pins = pinService.getPinsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(pins, "User pins retrieved successfully"));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Pin Service is running", null));
    }

    private UUID resolveRequesterId(UUID requesterIdFromAttr, UUID requesterIdFromHeader) {
        if (requesterIdFromAttr != null) {
            return requesterIdFromAttr;
        }
        if (requesterIdFromHeader != null) {
            return requesterIdFromHeader;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Id identity");
    }
}