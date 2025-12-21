package com.communityplatform.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.communityplatform.gateway.security.JwtTokenProvider;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Gateway filter that validates JWT tokens and injects user information as
 * headers.
 * 
 * Flow:
 * 1. Extract JWT from Authorization header
 * 2. Validate token
 * 3. Extract userId and username from token
 * 4. Add headers: X-User-Id, X-Username
 * 5. Forward request to downstream service
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        super(Config.class);
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Skip auth endpoints
            if (isAuthEndpoint(request.getPath().toString())) {
                log.debug("Skipping JWT validation for auth endpoint: {}", request.getPath());
                return chain.filter(exchange);
            }

            // Extract token from Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header");
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            // Validate token
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("Invalid JWT token");
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            // Extract userId and username
            String userId = jwtTokenProvider.getUserIdFromToken(token).toString();
            String username = jwtTokenProvider.getUsernameFromToken(token);

            // Add headers to request
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-Username", username)
                    .build();

            log.debug("JWT validated for user: {} (ID: {})", username, userId);

            // Continue with modified request
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    /**
     * Check if request is to auth endpoint (no JWT required).
     */
    private boolean isAuthEndpoint(String path) {
        return path.startsWith("/api/v1/auth/");
    }

    /**
     * Handle authentication errors.
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");

        String errorJson = String.format("{\"error\":\"%s\",\"message\":\"%s\"}",
                httpStatus.getReasonPhrase(), message);

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(errorJson.getBytes())));
    }

    public static class Config {
        // Configuration properties if needed
    }
}
