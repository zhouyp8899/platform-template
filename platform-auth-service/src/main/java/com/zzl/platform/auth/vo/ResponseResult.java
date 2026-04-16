package com.zzl.platform.auth.vo;

import lombok.Data;

/**
 * 统一响应结果
 */
@Data
public class ResponseResult<T> {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 时间戳
     */
    private Long timestamp;

    public ResponseResult() {
        this.timestamp = System.currentTimeMillis();
    }

    public ResponseResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> ResponseResult<T> success() {
        return new ResponseResult<>(200, "操作成功", null);
    }

    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>(200, "操作成功", data);
    }

    public static <T> ResponseResult<T> success(String message, T data) {
        return new ResponseResult<>(200, message, data);
    }

    public static <T> ResponseResult<T> error() {
        return new ResponseResult<>(500, "操作失败", null);
    }

    public static <T> ResponseResult<T> error(String message) {
        return new ResponseResult<>(500, message, null);
    }

    public static <T> ResponseResult<T> error(Integer code, String message) {
        return new ResponseResult<>(code, message, null);
    }

    public static <T> ResponseResult<T> unauthorized() {
        return new ResponseResult<>(401, "未认证", null);
    }

    public static <T> ResponseResult<T> unauthorized(String message) {
        return new ResponseResult<>(401, message, null);
    }

    public static <T> ResponseResult<T> forbidden() {
        return new ResponseResult<>(403, "无权限", null);
    }

    public static <T> ResponseResult<T> forbidden(String message) {
        return new ResponseResult<>(403, message, null);
    }

    public static <T> ResponseResult<T> badRequest() {
        return new ResponseResult<>(400, "请求参数错误", null);
    }

    public static <T> ResponseResult<T> badRequest(String message) {
        return new ResponseResult<>(400, message, null);
    }
}
