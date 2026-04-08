package com.pinlio.userservice.service;

import com.pinlio.userservice.dto.AuthResponse;
import com.pinlio.userservice.dto.UserLoginRequest;
import com.pinlio.userservice.dto.UserRegisterRequest;
import com.pinlio.userservice.dto.UserResponse;
import com.pinlio.userservice.entity.User;
import com.pinlio.userservice.entity.UserFollow;
import com.pinlio.userservice.repository.UserFollowRepository;
import com.pinlio.userservice.repository.UserRepository;
import com.pinlio.userservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    
    // Register new user
    public UserResponse register(UserRegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        String username = normalizeUsername(request.getUsername());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        User user = User.builder()
                .email(email)
                .username(username)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        
        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);
    }
    
    // Login user
    public AuthResponse login(UserLoginRequest request) {
        String email = normalizeEmail(request.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        } catch (DisabledException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is disabled");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is disabled");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        AuthResponse response = new AuthResponse(
                token,
                "Bearer",
                toUserResponse(user)
        );
        return response;
    }
    
    // Get user by ID
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toUserResponse(user);
    }
    
    // Update user
    public UserResponse updateUser(UUID xUserId, UUID userId, UserResponse request) {
        if (!xUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot update another user's profile");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }
        
        User updatedUser = userRepository.save(user);
        return toUserResponse(updatedUser);
    }
    
    // Follow user
    public void followUser(UUID followerId, UUID followingId) {
        if (followerId.equals(followingId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot follow yourself");
        }
        if (!userRepository.existsById(followingId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User to follow not found");
        }

        // Check if already following
        if (userFollowRepository.findByFollowerIdAndFollowingId(followerId, followingId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already following this user");
        }

        UserFollow userFollow = UserFollow.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();
        
        userFollowRepository.save(userFollow);
        
        // Update follow counts
        User following = userRepository.findById(followingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        following.setFollowersCount(Math.toIntExact(userFollowRepository.countByFollowingId(followingId)));
        userRepository.save(following);

        User follower = userRepository.findById(followerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        follower.setFollowingCount(Math.toIntExact(userFollowRepository.countByFollowerId(followerId)));
        userRepository.save(follower);
    }
    
    // Unfollow user
    public void unfollowUser(UUID followerId, UUID followingId) {
        UserFollow userFollow = userFollowRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not following this user"));

        userFollowRepository.delete(userFollow);

        // Update follow counts
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        following.setFollowersCount(Math.toIntExact(userFollowRepository.countByFollowingId(followingId)));
        userRepository.save(following);

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        follower.setFollowingCount(Math.toIntExact(userFollowRepository.countByFollowerId(followerId)));
        userRepository.save(follower);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        return username.trim();
    }
    
    // Helper method to convert User to UserResponse
    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .followersCount(user.getFollowersCount())
                .followingCount(user.getFollowingCount())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
