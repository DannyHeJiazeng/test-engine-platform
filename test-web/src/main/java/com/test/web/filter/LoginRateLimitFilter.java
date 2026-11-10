package com.test.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Single-instance login throttle. Uses the peer address, not untrusted forwarding headers. */
public class LoginRateLimitFilter extends OncePerRequestFilter {
    private record Window(long startedAt, int attempts) {}
    private final Map<String, Window> windows = new HashMap<>();

    private synchronized boolean allow(String address) {
        long now = System.currentTimeMillis();
        windows.entrySet().removeIf(entry -> now - entry.getValue().startedAt() >= 60_000);
        Window previous = windows.get(address);
        if (previous == null) {
            if (windows.size() >= 10_000) return false;
            windows.put(address, new Window(now, 1));
            return true;
        }
        if (previous.attempts() >= 10) return false;
        windows.put(address, new Window(previous.startedAt(), previous.attempts() + 1));
        return true;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        if ("POST".equals(request.getMethod()) && "/api/auth/login".equals(request.getServletPath())
                && !allow(request.getRemoteAddr())) {
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"登录请求过多，请稍后重试\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
