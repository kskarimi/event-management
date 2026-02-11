package com.kkarimi.eventmanagement.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
class CustomerApiRateLimitFilter extends OncePerRequestFilter {

    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final int TOO_MANY_REQUESTS_STATUS = 429;
    private static final String TOO_MANY_REQUESTS_BODY = "{\"error\":\"rate_limit_exceeded\"}";

    private final int requestsPerMinute;
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    CustomerApiRateLimitFilter(@Value("${app.rate-limit.customer-api.requests-per-minute:60}") int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!isCustomerApi(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = resolveClientKey(request);
        WindowCounter counter = counters.computeIfAbsent(key, ignored -> new WindowCounter(System.currentTimeMillis()));

        long now = System.currentTimeMillis();
        synchronized (counter) {
            if (now - counter.windowStartMillis >= WINDOW.toMillis()) {
                counter.windowStartMillis = now;
                counter.count.set(0);
            }

            if (counter.count.incrementAndGet() > requestsPerMinute) {
                response.setStatus(TOO_MANY_REQUESTS_STATUS);
                response.setContentType("application/json");
                response.getWriter().write(TOO_MANY_REQUESTS_BODY);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isCustomerApi(String uri) {
        return uri.startsWith("/api/attendees") || uri.startsWith("/api/registrations");
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static final class WindowCounter {
        private long windowStartMillis;
        private final AtomicInteger count = new AtomicInteger();

        private WindowCounter(long windowStartMillis) {
            this.windowStartMillis = windowStartMillis;
        }
    }
}
