package com.test.web.exception;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {

    private String errorCode;
    private String errorMessage;

    public BusinessException(String message) {
        super(message);
        this.errorMessage = message;
    }

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorMessage = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    // 静态工厂方法
    public static BusinessException of(String message) {
        return new BusinessException(message);
    }

    public static BusinessException of(String errorCode, String message) {
        return new BusinessException(errorCode, message);
    }

    // 常见业务异常
    public static BusinessException notFound(String resource) {
        return new BusinessException("NOT_FOUND", resource + " 不存在");
    }

    public static BusinessException alreadyExists(String resource) {
        return new BusinessException("ALREADY_EXISTS", resource + " 已存在");
    }

    public static BusinessException invalidParameter(String param) {
        return new BusinessException("INVALID_PARAMETER", "参数无效: " + param);
    }

    public static BusinessException operationFailed(String operation) {
        return new BusinessException("OPERATION_FAILED", operation + " 操作失败");
    }
}