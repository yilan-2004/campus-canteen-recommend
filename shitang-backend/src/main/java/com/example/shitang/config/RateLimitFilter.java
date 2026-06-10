package com.example.shitang.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简单接口限流 — 滑动窗口计数器
 * 对敏感接口(登录/注册/推荐)做 IP 级限流
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    /** IP -> 窗口信息 */
    private final ConcurrentHashMap<String, Window> counters = new ConcurrentHashMap<>();

    /** 每个窗口的时长(ms) */
    private static final long WINDOW_MS = 60_000; // 1 分钟
    /** 每个窗口最大请求数 */
    private static final int MAX_REQUESTS = 60;
    /** 登录/注册接口更严格 */
    private static final int AUTH_MAX_REQUESTS = 10;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        // 只对敏感接口限流
        if (!isSensitive(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        int limit = isAuth(uri) ? AUTH_MAX_REQUESTS : MAX_REQUESTS;
        String key = ip + ":" + (isAuth(uri) ? "auth" : "api");

        Window window = counters.compute(key, (k, v) -> {
            long now = System.currentTimeMillis();
            if (v == null || now - v.start > WINDOW_MS) {
                return new Window(now);
            }
            return v;
        });

        if (window.counter.incrementAndGet() > limit) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"data\":null}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSensitive(String uri) {
        return uri.startsWith("/api/auth/") || uri.startsWith("/api/recommend/");
    }

    private boolean isAuth(String uri) {
        return uri.startsWith("/api/auth/");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            ip = ip.split(",")[0].trim();
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private static class Window {
        final long start;
        final AtomicInteger counter = new AtomicInteger(0);
        Window(long start) { this.start = start; }
    }
}
