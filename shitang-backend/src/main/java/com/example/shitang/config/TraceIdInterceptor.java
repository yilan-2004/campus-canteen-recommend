package com.example.shitang.config;

import com.example.shitang.recommend.RecommendContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * 给推荐接口注入 traceId(从 header 取或生成)
 * 同时在请求结束清理 ThreadLocal
 */
@Component
public class TraceIdInterceptor implements HandlerInterceptor {

    public static final String HEADER = "X-Trace-Id";
    public static final String ATTR = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = request.getHeader(HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        request.setAttribute(ATTR, traceId);
        response.setHeader(HEADER, traceId);
        // 注意:userId/variant 在 Controller 内具体调用 service 前由 abTestRouter 决定
        // 这里仅设置 traceId
        RecommendContext.set(traceId, null, "A");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        RecommendContext.clear();
    }
}
