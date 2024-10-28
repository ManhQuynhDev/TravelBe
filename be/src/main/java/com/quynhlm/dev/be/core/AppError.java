package com.quynhlm.dev.be.core;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AppError {
    public enum ErrorCode {
        UNKNOWN,
        DATA_INVALID,
        ACCOUNT_EXIST,
        LOCATION_EXIST,
        STORY_NOT_FOUND,
        POST_NOT_FOUND,
        REVIEW_NOT_FOUND,
        GROUP_NOT_FOUND,
        GROUP_EXIST, MEMBER_NOT_FOUND
    }

    private ErrorCode code;
    private String message;

    public AppError(ErrorCode code, String message) {
        this.code = code;
        this.message = message;
    }
}
