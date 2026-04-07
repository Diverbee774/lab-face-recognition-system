package com.lab.face.interceptor;

import com.lab.face.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        if (path.equals("/api/admin/login") || path.equals("/api/admin/logout") || path.equals("/api/admin/info")) {
            return true;
        }

        if (path.startsWith("/api/")) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendUnauthorized(response, "未登录");
                return false;
            }

            String token = authHeader.replace("Bearer ", "");
            try {
                Claims claims = JwtUtils.parseToken(token);
                request.setAttribute("adminId", claims.get("adminId"));
                request.setAttribute("username", claims.getSubject());
            } catch (Exception e) {
                sendUnauthorized(response, "Token无效");
                return false;
            }
        }

        return true;
    }

    private void sendUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"" + msg + "\"}");
    }
}