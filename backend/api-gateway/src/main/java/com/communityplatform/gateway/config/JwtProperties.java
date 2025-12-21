package com.communityplatform.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * JWT Configuration Properties for API Gateway.
 * MUST match the configuration in user-service!
 */
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Validated
@Data
public class JwtProperties {

    /**
     * Secret key for signing JWT tokens.
     * Should be at least 256 bits (32 characters) for HS256.
     * 
     * IMPORTANT: Override with environment variable JWT_SECRET in production!
     */
    @NotBlank(message = "JWT secret must not be blank")
    private String secret = "dev-secret-key-do-not-use-in-production-must-be-at-least-256-bits-long!!";

    /**
     * JWT issuer claim (iss).
     * Identifies the principal that issued the JWT.
     */
    @NotBlank(message = "JWT issuer must not be blank")
    private String issuer = "community-platform";

    /**
     * JWT audience claim (aud).
     * Identifies the recipients that the JWT is intended for.
     */
    @NotBlank(message = "JWT audience must not be blank")
    private String audience = "community-platform-api";

    /**
     * Clock skew tolerance in seconds.
     * Allows for time differences between servers.
     * Default: 60 seconds
     */
    @Min(value = 0, message = "Clock skew must be non-negative")
    private long clockSkewSeconds = 60L;
}
