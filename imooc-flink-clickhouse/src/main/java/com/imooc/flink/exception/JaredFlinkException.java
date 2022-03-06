package com.imooc.flink.exception;

/**
 * @author jared
 * @since 2022/1/3
 */
public class JaredFlinkException extends RuntimeException {

    private final JaredFlinkErrorCode errorCode;

    public JaredFlinkException(JaredFlinkErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public JaredFlinkException(JaredFlinkErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public JaredFlinkErrorCode getErrorCode() {
        return errorCode;
    }
}
