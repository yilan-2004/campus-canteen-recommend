package com.example.shitang.recommend;

/**
 * 推荐请求上下文
 *
 * 在一次推荐请求周期内共享 traceId / 用户 / 变体等信息
 * 借助 ThreadLocal 透传到下游埋点、缓存、评估模块
 */
public final class RecommendContext {

    private static final ThreadLocal<ContextHolder> HOLDER = new ThreadLocal<>();

    private RecommendContext() {}

    public static void set(String traceId, Long userId, String variant) {
        HOLDER.set(new ContextHolder(traceId, userId, variant));
    }

    public static ContextHolder get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static String currentTraceId() {
        ContextHolder h = HOLDER.get();
        return h == null ? "-" : h.traceId;
    }

    public static Long currentUserId() {
        ContextHolder h = HOLDER.get();
        return h == null ? null : h.userId;
    }

    public static String currentVariant() {
        ContextHolder h = HOLDER.get();
        return h == null ? "A" : h.variant;
    }

    public record ContextHolder(String traceId, Long userId, String variant) {}
}
