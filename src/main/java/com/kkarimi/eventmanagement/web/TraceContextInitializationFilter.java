package com.kkarimi.eventmanagement.web;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
class TraceContextInitializationFilter extends OncePerRequestFilter {

    private final Tracer tracer;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            filterChain.doFilter(request, response);
            return;
        }

        Span span = tracer.nextSpan().name(request.getMethod() + " " + request.getRequestURI()).start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            filterChain.doFilter(request, response);
        } finally {
            span.end();
        }
    }
}
