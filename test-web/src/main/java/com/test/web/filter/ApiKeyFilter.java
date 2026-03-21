package com.test.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * API 密钥认证过滤器
 */
@Component
public class ApiKeyFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyFilter.class);

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String API_KEY = System.getenv("TEST_ENGINE_API_KEY");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestUri = httpRequest.getRequestURI();

        // 跳过不需要认证的路径
        if (isPublicPath(requestUri)) {
            chain.doFilter(request, response);
            return;
        }

        // 检查 API Key
        String providedKey = httpRequest.getHeader(API_KEY_HEADER);

        if (!isValidApiKey(providedKey)) {
            logger.warn("Invalid API key from IP: {}", getClientIp(httpRequest));
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("{\"success\": false, \"message\": \"Invalid API key\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String uri) {
        return uri.startsWith("/actuator") ||
               uri.startsWith("/static") ||
               uri.equals("/api/report/") ||
               uri.equals("/api/health");
    }

    private boolean isValidApiKey(String apiKey) {
        // 如果没有配置 API Key，则跳过验证
        if (API_KEY == null || API_KEY.isEmpty()) {
            return true;
        }
        return API_KEY.equals(apiKey);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}