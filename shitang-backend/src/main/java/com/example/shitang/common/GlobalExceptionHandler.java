package com.example.shitang.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.warn("[BizException] {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidException(Exception e) {
        String message = "请求参数错误";
        if (e instanceof MethodArgumentNotValidException ex && ex.getBindingResult().getFieldError() != null) {
            message = ex.getBindingResult().getFieldError().getDefaultMessage();
        }
        if (e instanceof BindException ex && ex.getBindingResult().getFieldError() != null) {
            message = ex.getBindingResult().getFieldError().getDefaultMessage();
        }
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException() {
        return Result.fail(ResultCode.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        // 打完整堆栈,便于排查(开发期)
        log.error("[UNHANDLED] {} : {}", e.getClass().getName(), e.getMessage(), e);
        String msg = e.getMessage();
        if (msg == null || msg.isEmpty()) {
            msg = e.getClass().getSimpleName() + ": " + summarizeCause(e);
        }
        return Result.fail(ResultCode.ERROR.getCode(), msg);
    }

    private String summarizeCause(Throwable t) {
        Throwable c = t.getCause();
        int depth = 0;
        while (c != null && depth++ < 5) {
            if (c.getMessage() != null) return c.getClass().getSimpleName() + ": " + c.getMessage();
            c = c.getCause();
        }
        return Arrays.toString(t.getStackTrace()).substring(0, Math.min(200, t.getStackTrace().length > 0 ? t.getStackTrace()[0].toString().length() : 0));
    }
}
