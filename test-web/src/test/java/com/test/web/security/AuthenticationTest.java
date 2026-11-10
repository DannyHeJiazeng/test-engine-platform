package com.test.web.security;

import com.test.web.config.SecurityConfig;
import com.test.web.controller.AuthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {AuthenticationTest.WebConfig.class, SecurityConfig.class, AuthController.class})
class AuthenticationTest {
    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    static class WebConfig {}

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("test-engine.auth.password-hash", () -> new BCryptPasswordEncoder().encode("correct-password"));
        registry.add("TEST_ENGINE_API_KEY", () -> "test-automation-key");
    }

    @Autowired WebApplicationContext context;
    MockMvc mvc;
    @BeforeEach void setup() { mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build(); }

    @Test void rejectsAnonymousAndWrongPassword() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").with(csrf()).param("username", "admin").param("password", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test void loginRotatesSessionAndLogoutInvalidatesIt() throws Exception {
        MockHttpSession before = new MockHttpSession();
        String oldId = before.getId();
        var result = mvc.perform(post("/api/auth/login").session(before).with(csrf())
                .param("username", "admin").param("password", "correct-password"))
                .andExpect(status().isOk()).andReturn();
        var session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotEquals(oldId, session.getId());
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"));
        mvc.perform(post("/api/auth/logout").session(session).with(csrf())).andExpect(status().isOk());
        assertTrue(session.isInvalid());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test void loginRequiresCsrf() throws Exception {
        mvc.perform(post("/api/auth/login").param("username", "admin").param("password", "correct-password"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test void apiKeysAreStatelessAndCannotLogIntoConsole() throws Exception {
        mvc.perform(get("/api/unknown").header("X-API-Key", "wrong")).andExpect(status().isUnauthorized());
        // Authentication succeeds and MVC returns 404 for this intentionally absent resource.
        var result = mvc.perform(post("/api/unknown").header("X-API-Key", "test-automation-key"))
                .andExpect(status().isNotFound()).andReturn();
        assertNull(result.getRequest().getSession(false));
        mvc.perform(get("/api/auth/me").header("X-API-Key", "test-automation-key"))
                .andExpect(status().isUnauthorized());
    }
}
