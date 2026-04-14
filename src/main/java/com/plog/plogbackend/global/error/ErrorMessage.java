package com.plog.plogbackend.global.error;

import lombok.Getter;

@Getter
public class ErrorMessage {
    private final String errorCode;
    private final String message;
    private final Object errorData;

    public ErrorMessage(ErrorType errorType,Object errorData) {
        this.errorCode = errorType.name();
        this.message = errorType.name();
        this.errorData = errorData;
    }
}
