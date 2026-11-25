package com.bookstore.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class ApiAuthenticationFilter extends OncePerRequestFilter {
    public static final String SESSION_ATTRIBUTE = ApiAuthenticationFilter.class.getName() + ".session";

    private final AuthSessionStore sessionStore;
    private final ObjectMapper objectMapper;

    public ApiAuthenticationFilter(AuthSessionStore sessionStore, ObjectMapper objectMapper) {
        this.sessionStore = sessionStore;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "/api/auth/login".equals(path)
                || "/api/auth/password-reset/request".equals(path)
                || "/api/auth/password-reset/verify".equals(path)
                || "/api/auth/password-reset/confirm".equals(path)
                || "/api/health".equals(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Phiên đăng nhập không hợp lệ hoặc đã hết hạn.");
            return;
        }

        AuthSessionStore.Session session;
        try {
            session = sessionStore.find(authorization.substring("Bearer ".length()));
        } catch (IllegalStateException ex) {
            writeJsonError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Hệ thống tạm thời không thể kết nối cơ sở dữ liệu. Vui lòng thử lại sau.");
            return;
        }

        if (session == null) {
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Phiên đăng nhập không hợp lệ hoặc đã hết hạn.");
            return;
        }

        request.setAttribute(SESSION_ATTRIBUTE, session);
        filterChain.doFilter(request, response);
    }

    private void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), Map.of("message", message));
    }
}
