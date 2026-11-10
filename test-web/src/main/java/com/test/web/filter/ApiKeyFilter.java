package com.test.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class ApiKeyFilter extends OncePerRequestFilter {
    private final String key;
    public ApiKeyFilter(String key) { this.key = key; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String supplied = request.getHeader("X-API-Key");
        if (!key.isBlank() && supplied != null && MessageDigest.isEqual(
                key.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new UsernamePasswordAuthenticationToken("automation", null,
                    AuthorityUtils.createAuthorityList("ROLE_AUTOMATION")));
            SecurityContextHolder.setContext(context);
        }
        chain.doFilter(request, response);
    }
}
