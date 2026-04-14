package com.plog.plogbackend.global.error;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final ErrorType errorType;
    private final Object errorData;

    public AppException(ErrorType errorType){
        this.errorType = errorType;
        this.errorData = null;
    }

    public AppException(ErrorType errorType,Object errorData){
        this.errorType = errorType;
        this.errorData = errorData;
    }
}
