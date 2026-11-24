package com.mental.config;

import com.mental.security.UserContext;
import com.mental.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Resource
    private JwtUtil jwtUtil;

    // 不需要验证 token 的路径
    private static final String[] PUBLIC_PATHS = {
            "/api/user/login",
            "/api/user/register",
            "/api/userAnswer/add",
            "/api/ai/chat"
    };

    @Override
    public boolean preHandle(HttpServletRequest r, HttpServletResponse resp, Object handler) throws Exception {
        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(r.getMethod())) {
            return true;
        }

        String path = r.getRequestURI();

        // 检查是否是公开路径
        for (String publicPath : PUBLIC_PATHS) {
            if (path.equals(publicPath) || path.startsWith(publicPath)) {
                return true;
            }
        }

        // 获取 token（支持多种 header 名称）
        String token = r.getHeader("token");
        if (token == null) {
            token = r.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
        }

        // 非公开路径必须有 token
        if (token == null || token.isEmpty()) {
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"code\":401,\"msg\":\"未登录\"}");
            return false;
        }

        try {
            Claims claims = jwtUtil.parse(token);
            Long uid = claims.get("userId", Long.class);
            String role = claims.get("role", String.class);
            UserContext.set(uid, role);
            System.out.println("JWT 验证成功，用户ID: " + uid + ", 角色: " + role);
            return true;
        } catch (Exception e) {
            System.err.println("JWT 验证失败: " + e.getMessage());
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"code\":401,\"msg\":\"token无效或已过期\"}");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest r, HttpServletResponse resp, Object handler, Exception e) throws Exception {
        UserContext.clear();
    }
}