package com.ats.platform;

public class BaseSystemException extends Exception {
    public BaseSystemException(String message) {
        super(message);
    }

    public BaseSystemException(Exception e) {
        super(e);
    }

    public BaseSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
