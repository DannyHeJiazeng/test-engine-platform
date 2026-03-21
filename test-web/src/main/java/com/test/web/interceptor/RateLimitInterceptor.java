package com.test.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 限流拦截器
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private static final long REQUESTS_PER_MINUTE = 100;

    private final AtomicLong counter = new AtomicLong(0);
    private long lastResetTime = System.currentTimeMillis();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        resetCounterIfNeeded();

        long currentCount = counter.incrementAndGet();

        if (currentCount > REQUESTS_PER_MINUTE) {
            logger.warn("Rate limit exceeded for IP: {}", getClientIp(request));
            response.setStatus(429); // SC_TOO_MANY_REQUESTS
            response.getWriter().write("{\"success\": false, \"message\": \"Rate limit exceeded\"}");
            return false;
        }

        return true;
    }

    private void resetCounterIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastResetTime >= TimeUnit.MINUTES.toMillis(1)) {
            counter.set(0);
            lastResetTime = currentTime;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}