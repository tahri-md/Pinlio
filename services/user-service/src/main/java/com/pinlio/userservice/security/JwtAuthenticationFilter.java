package com.pinlio.userservice.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String REQUEST_USER_ID_ATTR = "X-User-Id";
    private static final String GATEWAY_USER_ID_HEADER = "X-User-Id";
    
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        try {
            UUID gatewayUserId = resolveGatewayUserId(request);
            if (gatewayUserId != null) {
                request.setAttribute(REQUEST_USER_ID_ATTR, gatewayUserId);
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            gatewayUserId.toString(),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            String authHeader = request.getHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (jwtUtil.isTokenValid(token)
                        && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UUID userId = jwtUtil.extractUserId(token);
                    request.setAttribute(REQUEST_USER_ID_ATTR, userId);

                    // Load user details from database using the service
                    UserDetails userDetails = userDetailsService.loadUserById(userId);

                    // Create authentication token with user details and authorities
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, 
                            userDetails.getAuthorities()
                        );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Cannot set user authentication", e);
        } catch (Exception e) {
            logger.error("Error processing JWT token", e);
        }
        
        filterChain.doFilter(request, response);
    }

    private UUID resolveGatewayUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader(GATEWAY_USER_ID_HEADER);
        if (userIdHeader == null || userIdHeader.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(userIdHeader.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
