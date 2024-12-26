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
        ACCOUNT_NOT_FOUND,
        LOCATION_EXIST,
        STORY_NOT_FOUND,
        POST_NOT_FOUND,
        REVIEW_NOT_FOUND,
        GROUP_NOT_FOUND,
        GROUP_EXIST, MEMBER_NOT_FOUND,
        COMMENT_NOT_FOUND,
        TRAVEL_PLAN_NOT_FOUND,
        ACTIVITIES_EXIST,
        ACTIVITIES_NOT_FOUND,
        SHARE_NOT_FOUND,
        REPLY_NOT_FOUND,
        REPORT_EXIST,
        REPORT_NOT_FOUND,
        TAG_NOT_FOUND,        
        METHOD_NOT_ALLOWED,
        USER_ALREADY,
        NOTIFICATION_NOT_FOUND,
        LOCATION_NOT_FOUND,
        REVIEW_EXIST,
        ACCOUNT_DISABLED
    }

    private ErrorCode code;
    private String message;

    public AppError(ErrorCode code, String message) {
        this.code = code;
        this.message = message;
    }
}
