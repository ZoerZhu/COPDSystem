package com.copd.interceptor;

import com.alibaba.fastjson2.JSON;
import com.copd.annotation.RequireRole;
import com.copd.common.Result;
import com.copd.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 不需要拦截的路径
     */
    private static final String[] EXCLUDE_PATHS = {
        "/api/user/login",
        "/api/user/register",
        "/api/user/checkUsername",
        "/druid/**",
        "/error"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是映射到方法，直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 检查是否需要跳过拦截
        String requestURI = request.getRequestURI();
        for (String excludePath : EXCLUDE_PATHS) {
            if (pathMatcher.match(excludePath, requestURI)) {
                return true;
            }
        }

        // 获取Token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            sendUnauthorized(response, "请先登录");
            return false;
        }

        // 去除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证Token
        if (!jwtUtil.validateToken(token)) {
            sendUnauthorized(response, "登录已过期，请重新登录");
            return false;
        }

        // 获取方法上的注解
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);

        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        // 如果没有注解，默认需要登录
        if (requireRole == null) {
            // 继续处理，设置用户信息到request属性
            request.setAttribute("userId", jwtUtil.getUserIdFromToken(token));
            request.setAttribute("username", jwtUtil.getUsernameFromToken(token));
            request.setAttribute("role", jwtUtil.getRoleFromToken(token));
            return true;
        }

        // 获取用户角色
        String userRole = jwtUtil.getRoleFromToken(token);

        // 检查是否需要管理员权限
        if (requireRole.admin()) {
            if (!"ADMIN".equals(userRole)) {
                sendForbidden(response, "需要管理员权限");
                return false;
            }
        }

        // 检查角色权限
        String[] allowedRoles = requireRole.value();
        if (allowedRoles != null && allowedRoles.length > 0) {
            boolean hasPermission = Arrays.asList(allowedRoles).contains(userRole);
            if (!hasPermission) {
                sendForbidden(response, "您没有权限访问该接口");
                return false;
            }
        }

        // 设置用户信息到request属性
        request.setAttribute("userId", jwtUtil.getUserIdFromToken(token));
        request.setAttribute("username", jwtUtil.getUsernameFromToken(token));
        request.setAttribute("role", userRole);

        return true;
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(Result.error(401, message)));
    }

    private void sendForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(Result.error(403, message)));
    }
}
