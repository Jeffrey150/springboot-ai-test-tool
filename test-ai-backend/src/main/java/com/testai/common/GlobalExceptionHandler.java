package com.testai.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常，请求地址：{}，异常信息：{}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        log.error("参数校验异常，请求地址：{}，异常信息：{}", request.getRequestURI(), e.getMessage());
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return Result.error(400, message);
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常，请求地址：{}，异常信息：{}", request.getRequestURI(), e.getMessage(), e);
        return Result.error("系统繁忙，请稍后再试");
    }
}
