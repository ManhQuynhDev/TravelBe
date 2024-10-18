package com.quynhlm.dev.be.core;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AppError {
    public enum ErrorCode {
        UNKNOW ,
        DATA_INVALID , ACCOUNT_EXIST , LOCATION_EXIST
    }    
    private ErrorCode code;
    private String message;

    public AppError(ErrorCode code, String message) {
        this.code = code;
        this.message = message;
    }
}
