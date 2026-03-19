package com.ceo.dashboard.util;

/**
 * API 响应统一格式工具类
 */
public class ApiResponse<T> {
    
    private int code;
    private String message;
    private T data;
    
    // 私有构造函数
    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    // 成功响应
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(200, message, data);
    }
    
    // 错误响应
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null);
    }
    
    public static <T> ApiResponse<T> error(String message, int code) {
        return new ApiResponse<>(code, message, null);
    }
    
    public static <T> ApiResponse<T> error(T data, String message, int code) {
        return new ApiResponse<>(code, message, data);
    }
    
    // Getter 和 Setter
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
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