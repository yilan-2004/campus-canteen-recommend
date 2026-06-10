package com.example.shitang.utils;

/**
 * 输入清洗工具 — 防 XSS
 */
public final class SanitizeUtils {
    private SanitizeUtils() {}

    /**
     * 转义 HTML 特殊字符,防止 XSS 注入
     */
    public static String sanitizeHtml(String input) {
        if (input == null) return null;
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    /**
     * 去除前后空白并压缩连续空格
     */
    public static String cleanText(String input) {
        if (input == null) return null;
        return input.strip().replaceAll("\\s+", " ");
    }
}
