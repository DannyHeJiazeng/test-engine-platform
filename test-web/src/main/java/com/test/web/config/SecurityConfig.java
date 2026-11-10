package com.test.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.web.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.test.web.filter.ApiKeyFilter;

@Configuration
public class SecurityConfig {
    @Bean
    public UserDetailsService users(@Value("${test-engine.auth.username:admin}") String username,
            @Value("${test-engine.auth.password-hash:}") String hash) {
        if (!hash.matches("\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}")) {
            throw new IllegalStateException("Set TEST_ENGINE_ADMIN_PASSWORD_HASH to a BCrypt password hash");
        }
        return new InMemoryUserDetailsManager(User.withUsername(username).password(hash).roles("ADMIN").build());
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    private void json(HttpServletResponse response, int status, Object body) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        new ObjectMapper().writeValue(response.getWriter(), body);
    }

    // Automation requests use a separate, stateless chain; an invalid key never falls back to a session.
    @Bean
    @Order(1)
    public SecurityFilterChain automation(HttpSecurity http,
            @Value("${TEST_ENGINE_API_KEY:}") String key) throws Exception {
        http.securityMatcher(request -> request.getRequestURI().startsWith("/api/")
                && !request.getRequestURI().startsWith("/api/auth/")
                && request.getHeader("X-API-Key") != null)
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(
                    org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
            .addFilterBefore(new ApiKeyFilter(key), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .exceptionHandling(errors -> errors.authenticationEntryPoint((req, res, ex) ->
                    json(res, 401, ApiResponse.error("未认证"))));
        return http.build();
    }

    @Bean
    public SecurityFilterChain console(HttpSecurity http) throws Exception {
        http.addFilterBefore(new com.test.web.filter.LoginRateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/csrf", "/api/auth/login").permitAll()
                .requestMatchers("/api/**", "/actuator/**", "/h2-console/**").authenticated()
                .anyRequest().permitAll())
            .formLogin(login -> login.loginPage("/login").loginProcessingUrl("/api/auth/login")
                .successHandler((req, res, auth) -> json(res, 200, ApiResponse.success(
                        java.util.Map.of("username", auth.getName()))))
                .failureHandler((req, res, ex) -> json(res, 401, ApiResponse.error("账号或密码错误"))))
            .logout(logout -> logout.logoutUrl("/api/auth/logout")
                .invalidateHttpSession(true).deleteCookies("JSESSIONID")
                .logoutSuccessHandler((req, res, auth) -> json(res, 200, ApiResponse.success(null))))
            .requestCache(cache -> cache.disable())
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((req, res, ex) -> json(res, 401, ApiResponse.error("请先登录")))
                .accessDeniedHandler((req, res, ex) -> json(res, 403, ApiResponse.error("请求校验失败，请刷新重试"))));
        return http.build();
    }
}
