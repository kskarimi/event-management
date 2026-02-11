package com.kkarimi.eventmanagement.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
class HttpRequestLoggingFilter extends OncePerRequestFilter {

    private final int maxBodyLength;

    HttpRequestLoggingFilter(@Value("${app.logging.http.max-body-length:2000}") int maxBodyLength) {
        this.maxBodyLength = maxBodyLength;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        long start = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestWrapper, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            String body = extractBody(requestWrapper);
            String query = requestWrapper.getQueryString();
            String path = query == null ? requestWrapper.getRequestURI() : requestWrapper.getRequestURI() + "?" + query;
            log.info(
                    "HTTP {} {} status={} durationMs={} body={}",
                    requestWrapper.getMethod(),
                    path,
                    response.getStatus(),
                    duration,
                    body
            );
        }
    }

    private String extractBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return "\"\"";
        }

        String body = new String(content, StandardCharsets.UTF_8).trim();
        if (body.length() > maxBodyLength) {
            return body.substring(0, maxBodyLength) + "...(truncated)";
        }
        return body;
    }
}
