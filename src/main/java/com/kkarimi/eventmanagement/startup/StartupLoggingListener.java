package com.kkarimi.eventmanagement.startup;

import java.net.InetAddress;
import java.time.Duration;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class StartupLoggingListener {

    private static final String DEFAULT_HTTP_PORT = "8080";

    private final Environment environment;
    private final Tracer tracer;

    @Value("${spring.application.name:event-management}")
    private String applicationName;

    @Value("${management.endpoints.web.base-path:/actuator}")
    private String actuatorBasePath;

    @Value("${app.rate-limit.customer-api.requests-per-minute:60}")
    private int customerApiRateLimit;

    StartupLoggingListener(Environment environment, Tracer tracer) {
        this.environment = environment;
        this.tracer = tracer;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        Span startupSpan = tracer.nextSpan().name("application-startup").start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(startupSpan)) {
            logStartupSummary(event);
        } finally {
            startupSpan.end();
        }
    }

    private void logStartupSummary(ApplicationReadyEvent event) {
        Duration startupDuration = event.getTimeTaken();
        String startupTime = startupDuration == null ? "unknown" : startupDuration.toMillis() + " ms";
        String[] profiles = environment.getActiveProfiles();
        String activeProfiles = profiles.length == 0 ? "default" : String.join(", ", profiles);

        log.info("============================================================");
        log.info("Application '{}' is ready", applicationName);
        log.info("Startup time: {}", startupTime);
        log.info("JVM: {} ({})", System.getProperty("java.version"), System.getProperty("java.vendor"));
        log.info("PID: {}", ProcessHandle.current().pid());
        log.info("Active profiles: {}", activeProfiles);
        log.info("HTTP base URL: {}", resolveBaseUrl());
        log.info("Actuator health: {}{}", resolveBaseUrl(), actuatorBasePath + "/health");
        log.info("Datasource: {}", environment.getProperty("spring.datasource.url"));
        log.info("Redis: {}:{}", environment.getProperty("spring.data.redis.host"), environment.getProperty("spring.data.redis.port"));
        log.info("MongoDB: {}", environment.getProperty("spring.data.mongodb.uri"));
        log.info("Customer API rate limit: {} req/min", customerApiRateLimit);
        log.info("============================================================");
    }

    private String resolveBaseUrl() {
        String port = environment.getProperty("server.port", DEFAULT_HTTP_PORT);
        String contextPath = environment.getProperty("server.servlet.context-path", "");
        String host = resolveHostAddress();
        return "http://" + host + ":" + port + contextPath;
    }

    private String resolveHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception exception) {
            log.debug("Could not resolve local host address, using localhost", exception);
            return "localhost";
        }
    }
}
