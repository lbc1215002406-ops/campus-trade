package com.example.demo.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String uri = request.getRequestURI();
        // 公开可访问：商品列表、商品详情、分类
        if (uri.startsWith("/api/products") && "GET".equalsIgnoreCase(request.getMethod()))
            return true;
        if (uri.startsWith("/api/categories"))
            return true;

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
            return false;
        }
        try {
            Claims claims = JwtUtil.parse(auth.substring(7));
            request.setAttribute("userId", JwtUtil.getUserId(claims));
            request.setAttribute("role", JwtUtil.getRole(claims));

            // 管理员接口校验
            if (uri.startsWith("/api/admin") || uri.contains("/admin/")) {
                Integer role = JwtUtil.getRole(claims);
                if (role == null || role != 1) {
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"无管理员权限\"}");
                    return false;
                }
            }
            return true;
        } catch (ExpiredJwtException e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"登录已过期，请重新登录\"}");
            return false;
        } catch (JwtException e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"无效的登录凭证\"}");
            return false;
        }
    }
}
