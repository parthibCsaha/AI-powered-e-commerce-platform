package com.backend.security;

import com.backend.config.RateLimitConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig rateLimitConfig;

    private final ConcurrentHashMap<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String clientIp = resolveClientIp(request);
        String path = request.getRequestURI();
        String tier = resolveTier(path);

        String bucketKey = tier + ":" + clientIp;
        Bucket bucket = bucketCache.computeIfAbsent(bucketKey, k -> createBucket(tier));
    
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
 
        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            log.warn("Rate limit exceeded for IP: {} on tier: {} (path: {})", clientIp, tier, path);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.addHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.addHeader("X-Rate-Limit-Remaining", "0");

            String json = "{\"message\":\"Rate limit exceeded. Please try again in " + retryAfterSeconds + " seconds.\"}";
            response.getWriter().write(json);
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveTier(String path) {
        if (path.startsWith("/api/auth")) {
            return "auth";
        } else if (path.startsWith("/api/ai")) {
            return "ai";
        }
        return "general";
    }

    private Bucket createBucket(String tier) {
        RateLimitConfig.TierConfig config = switch (tier) {
            case "auth" -> rateLimitConfig.getAuth();
            case "ai" -> rateLimitConfig.getAi();
            default -> rateLimitConfig.getGeneral();
        };

        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(config.getCapacity())
                .refillGreedy(config.getCapacity(), Duration.ofMinutes(config.getMinutes()))
                .build();

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }
}

