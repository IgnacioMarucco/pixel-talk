package com.communityplatform.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway Application - Routes and secures access to all microservices.
 * 
 * Features:
 * - Centralized JWT validation
 * - Request routing to microservices
 * - CORS configuration
 * - Internal header injection (X-User-Id, X-Username)
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
