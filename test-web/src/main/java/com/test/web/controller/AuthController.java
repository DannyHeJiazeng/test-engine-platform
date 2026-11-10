package com.test.web.controller;

import com.test.web.dto.ApiResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @GetMapping("/csrf")
    public ApiResponse<Map<String, String>> csrf(CsrfToken token) {
        return ApiResponse.success(Map.of("token", token.getToken(), "headerName", token.getHeaderName()));
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, String>> me(Principal principal) {
        return ApiResponse.success(Map.of("username", principal.getName()));
    }
}
