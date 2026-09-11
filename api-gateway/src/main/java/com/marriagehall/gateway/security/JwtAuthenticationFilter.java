package com.marriagehall.gateway.security;


import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Allow CORS preflight requests through without authentication check
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();
        log.debug("Incoming request: method={}, path={}", method, path);

        boolean isPublic = isPublicEndpoint(path, method);

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // If it's a public endpoint and no token is provided, allow guest access
        if (isPublic && (authHeader == null || !authHeader.startsWith("Bearer "))) {
            return chain.filter(exchange);
        }

        // If not public and no valid header -> reject with 401
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("Rejecting {} {} - missing or malformed Authorization header", method, path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        // Single cryptographic parse of claims for maximum performance & correctness
        Claims claims;
        try {
            claims = jwtUtil.extractClaims(token);
        } catch (Exception ex) {
            log.debug("Rejecting {} {} - token failed validation: {}", method, path, ex.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        Object userIdObj = claims.get("userId");
        Object roleObj = claims.get("role");
        String userId = userIdObj != null ? userIdObj.toString() : "";
        String role = roleObj != null ? roleObj.toString() : "";
        String email = claims.getSubject() != null ? claims.getSubject() : "";

        log.debug("Authenticated request to {} as userId={} role={}", path, userId, role);

        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .header("X-User-Id", userId)
                .header("X-Role", role)
                .header("X-Email", email)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private boolean isPublicEndpoint(String path, HttpMethod method) {
        // Always public auth endpoints & developer tools
        if (path.startsWith("/api/auth/signup")
                || path.startsWith("/api/auth/login")
                || path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/favicon.ico")) {
            return true;
        }

        // Public read-only GET endpoints (Guest Discovery / Browsing)
        if (method == HttpMethod.GET) {
            // Halls catalog, details, search, cities (except private vendor list)
            if (path.startsWith("/api/halls") && !path.startsWith("/api/halls/vendor/")) {
                return true;
            }
            // Reviews & ratings for halls
            if (path.startsWith("/api/reviews")) {
                return true;
            }
            // Availability checks
            if (path.startsWith("/api/bookings/availability") || path.startsWith("/api/bookings/check-availability")) {
                return true;
            }
        }

        return false;
    }
}