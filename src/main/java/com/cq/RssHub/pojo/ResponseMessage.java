package com.cq.RssHub.pojo;

import org.springframework.http.HttpStatus;

/**
 * 统一响应消息类
 * @param <T> 数据类型
 */
public class ResponseMessage<T> {
    private Integer code;
    private Boolean success;
    private String message;
    private T data;

    /**
     * 完整构造函数
     */
    public ResponseMessage(Integer code, Boolean success, String message, T data) {
        this.code = code;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * 简化构造函数（适用于前端使用success作为判断标准的情况）
     */
    public ResponseMessage(Boolean success, String message, T data) {
        this.code = success ? HttpStatus.OK.value() : HttpStatus.INTERNAL_SERVER_ERROR.value();
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // ===== 成功响应的静态工厂方法 =====

    /**
     * 返回成功响应，包含数据
     */
    public static <T> ResponseMessage<T> success(T data) {
        return new ResponseMessage<>(HttpStatus.OK.value(), true, "操作成功", data);
    }

    /**
     * 返回成功响应，包含数据和自定义消息
     */
    public static <T> ResponseMessage<T> success(String message, T data) {
        return new ResponseMessage<>(HttpStatus.OK.value(), true, message, data);
    }

    /**
     * 返回成功响应，不包含数据
     */
    public static <T> ResponseMessage<T> success() {
        return new ResponseMessage<>(HttpStatus.OK.value(), true, "操作成功", null);
    }

    /**
     * 返回成功响应，包含自定义消息
     */
    public static ResponseMessage<String> successMsg(String message) {
        return new ResponseMessage<>(HttpStatus.OK.value(), true, message, null);
    }

    // ===== 失败响应的静态工厂方法 =====

    /**
     * 返回失败响应，包含错误消息
     */
    public static <T> ResponseMessage<T> error(String message) {
        return new ResponseMessage<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), false, message, null);
    }

    /**
     * 返回失败响应，包含自定义状态码和错误消息
     */
    public static <T> ResponseMessage<T> error(Integer code, String message) {
        return new ResponseMessage<>(code, false, message, null);
    }

    /**
     * 返回失败响应，包含自定义状态码、错误消息和数据
     */
    public static <T> ResponseMessage<T> error(Integer code, String message, T data) {
        return new ResponseMessage<>(code, false, message, data);
    }

    /**
     * 返回未授权响应
     */
    public static <T> ResponseMessage<T> unauthorized(String message) {
        return new ResponseMessage<>(HttpStatus.UNAUTHORIZED.value(), false, message, null);
    }

    /**
     * 返回参数错误响应
     */
    public static <T> ResponseMessage<T> badRequest(String message) {
        return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), false, message, null);
    }

    /**
     * 返回资源未找到响应
     */
    public static <T> ResponseMessage<T> notFound(String message) {
        return new ResponseMessage<>(HttpStatus.NOT_FOUND.value(), false, message, null);
    }

    // ===== 分页响应的工厂方法 =====

    /**
     * 创建分页响应（可以接收PageInfo等分页对象）
     */
    public static <T> ResponseMessage<T> page(T pageData) {
        return new ResponseMessage<>(HttpStatus.OK.value(), true, "查询成功", pageData);
    }

    // ===== Getter和Setter方法 =====

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
