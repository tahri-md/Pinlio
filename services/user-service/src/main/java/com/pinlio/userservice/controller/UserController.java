package com.pinlio.userservice.controller;

import com.pinlio.userservice.dto.*;
import com.pinlio.userservice.security.JwtAuthenticationFilter;
import com.pinlio.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody UserRegisterRequest request) {
        UserResponse createdUser = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdUser, "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody UserLoginRequest request) {
        AuthResponse auth = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success(auth, "Login successful"));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(
            @PathVariable UUID userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user, "User profile retrieved"));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @PathVariable UUID userId,
            @RequestAttribute(JwtAuthenticationFilter.REQUEST_USER_ID_ATTR) UUID xUserId,
            @Valid @RequestBody UserResponse request) {

        if (!xUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot update another user's profile");
        }

        UserResponse updated = userService.updateUser(xUserId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "User profile updated"));
    }

    @PostMapping("/{followerId}/follow/{followingId}")
    public ResponseEntity<ApiResponse<Void>> followUser(
            @PathVariable UUID followerId,
            @PathVariable UUID followingId,
            @RequestAttribute(JwtAuthenticationFilter.REQUEST_USER_ID_ATTR) UUID xUserId) {

        if (!xUserId.equals(followerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot follow on behalf of another user");
        }

        userService.followUser(xUserId, followingId);
        return ResponseEntity.ok(ApiResponse.success(null, "Followed user successfully"));
    }

    @DeleteMapping("/{followerId}/follow/{followingId}")
    public ResponseEntity<ApiResponse<Void>> unfollowUser(
            @PathVariable UUID followerId,
            @PathVariable UUID followingId,
            @RequestAttribute(JwtAuthenticationFilter.REQUEST_USER_ID_ATTR) UUID xUserId) {

        if (!xUserId.equals(followerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot unfollow on behalf of another user");
        }

        userService.unfollowUser(xUserId, followingId);
        return ResponseEntity.ok(ApiResponse.success(null, "Unfollowed user successfully"));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("User Service is running", null));
    }
}