package com.communityplatform.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.communityplatform.gateway.filter.JwtAuthenticationFilter;

/**
 * Gateway routing configuration.
 * 
 * Routes:
 * - /api/v1/auth/** → user-service (public, no JWT)
 * - /api/v1/users/** → user-service (protected, JWT required)
 * - /api/v1/follows/** → user-service (protected, JWT required)
 * - /api/v1/posts/** → content-service (protected, JWT required)
 * - /api/v1/comments/** → content-service (protected, JWT required)
 */
@Configuration
public class GatewayConfig {

        @Bean
        public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                        JwtAuthenticationFilter jwtAuthFilter) {
                return builder.routes()
                                // Auth endpoints (public - no JWT)
                                .route("auth", r -> r
                                                .path("/api/v1/auth/**")
                                                .uri("http://127.0.0.1:8081"))

                                // User service endpoints (protected)
                                .route("users", r -> r
                                                .path("/api/v1/users/**", "/api/v1/follows/**")
                                                .filters(f -> f.filter(jwtAuthFilter
                                                                .apply(new JwtAuthenticationFilter.Config())))
                                                .uri("http://127.0.0.1:8081"))

                                // Content service endpoints (protected)
                                .route("content", r -> r
                                                .path("/api/v1/posts/**", "/api/v1/comments/**")
                                                .filters(f -> f.filter(jwtAuthFilter
                                                                .apply(new JwtAuthenticationFilter.Config())))
                                                .uri("http://127.0.0.1:8082"))

                                .build();
        }
}
