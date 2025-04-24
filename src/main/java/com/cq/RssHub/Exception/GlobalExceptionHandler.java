package com.cq.RssHub.Exception;

import com.cq.RssHub.pojo.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 全局异常处理
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 日志
    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(Exception.class)
    public ResponseMessage<String> handleException(Exception e) {

        logger.error("全局异常捕获：" + e.getMessage()); // 记录异常信息
        return ResponseMessage.error(StringUtils.hasLength(e.getMessage())? e.getMessage() : "服务器异常"); // 返回错误信息
    }
}
