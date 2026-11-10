package com.test.web.security;

import com.test.web.filter.LoginRateLimitFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimitFilterTest {
    @Test void throttlesLoginWithoutBlockingOtherRequestsOrPeers() throws Exception {
        var filter = new LoginRateLimitFilter();
        for (int i = 0; i < 11; i++) {
            var response = attempt(filter, "/api/auth/login", "127.0.0.1");
            assertEquals(i < 10 ? 200 : 429, response.getStatus());
        }
        assertEquals(200, attempt(filter, "/api/auth/login", "127.0.0.2").getStatus());
        assertEquals(200, attempt(filter, "/api/auth/logout", "127.0.0.1").getStatus());
    }

    private MockHttpServletResponse attempt(LoginRateLimitFilter filter, String path, String address) throws Exception {
        var request = new MockHttpServletRequest("POST", path);
        request.setServletPath(path);
        request.setRemoteAddr(address);
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> {});
        return response;
    }
}
