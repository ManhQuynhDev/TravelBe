package com.quynhlm.dev.be.core.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.quynhlm.dev.be.core.AppError;
import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.core.AppError.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;

@SuppressWarnings({ "rawtypes", "unchecked" })
@ControllerAdvice
public class AppExceptionHandler {
    @ExceptionHandler(value = { UserAccountExistingException.class, UserAccountNotFoundException.class,
            LocationExistingException.class, StoryNotFoundException.class, PostNotFoundException.class,
            ReViewNotFoundException.class, GroupExistingException.class, GroupNotFoundException.class,
            MemberNotFoundException.class, CommentNotFoundException.class, TravelPlanNotFoundException.class,
            ActivitiesExistingException.class, ActivitiesNotFoundException.class, ShareNotFoundException.class,
            ReplyNotFoundException.class, ReportExistingException.class, TagNotFoundException.class })
    public ResponseEntity<ResponseObject> handleCustomExceptions(RuntimeException ex, HttpServletRequest request) {
        ResponseObject response = new ResponseObject();
        response.setMessage("Data is invalid.");
        response.setStatus(false);

        AppError.ErrorCode errorCode;

        switch (ex.getClass().getSimpleName()) {
            case "UserAccountExistingException":
                errorCode = AppError.ErrorCode.ACCOUNT_EXIST;
                break;
            case "UserAccountNotFoundException":
                errorCode = AppError.ErrorCode.ACCOUNT_NOT_FOUND;
                break;
            case "LocationExistingException":
                errorCode = AppError.ErrorCode.LOCATION_EXIST;
                break;
            case "StoryNotFoundException":
                errorCode = AppError.ErrorCode.STORY_NOT_FOUND;
                break;
            case "PostNotFoundException":
                errorCode = AppError.ErrorCode.POST_NOT_FOUND;
                break;
            case "ReViewNotFoundException":
                errorCode = AppError.ErrorCode.REVIEW_NOT_FOUND;
                break;
            case "GroupNotFoundException":
                errorCode = AppError.ErrorCode.GROUP_NOT_FOUND;
                break;
            case "GroupExistingException":
                errorCode = AppError.ErrorCode.GROUP_EXIST;
                break;
            case "MemberNotFoundException":
                errorCode = AppError.ErrorCode.MEMBER_NOT_FOUND;
                break;
            case "CommentNotFoundException":
                errorCode = AppError.ErrorCode.COMMENT_NOT_FOUND;
                break;
            case "TravelPlanNotFoundException":
                errorCode = AppError.ErrorCode.TRAVEL_PLAN_NOT_FOUND;
                break;
            case "ActivitiesExistingException":
                errorCode = AppError.ErrorCode.ACTIVITIES_EXIST;
                break;
            case "ActivitiesNotFoundException":
                errorCode = AppError.ErrorCode.ACTIVITIES_NOT_FOUND;
                break;
            case "ShareNotFoundException":
                errorCode = AppError.ErrorCode.SHARE_NOT_FOUND;
                break;
            case "ReplyNotFoundException":
                errorCode = AppError.ErrorCode.REPLY_NOT_FOUND;
                break;
            case "ReportExistingException":
                errorCode = AppError.ErrorCode.REPORT_EXIST;
                break;
            case "ReportNotFoundException":
                errorCode = AppError.ErrorCode.REPORT_NOT_FOUND;
                break;
            case "TagNotFoundException":
                errorCode = AppError.ErrorCode.REPORT_NOT_FOUND;
                break;
            default:
                errorCode = AppError.ErrorCode.UNKNOWN;
                break;
        }

        response.setError(new AppError(errorCode, ex.getMessage()));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {

        AppError error = new AppError(AppError.ErrorCode.METHOD_NOT_ALLOWED,
                ex.getMessage());
        ResponseObject<Boolean> responseObject = new ResponseObject<>();
        responseObject.setStatus(false);
        responseObject.setError(error);
        responseObject.setMessage("Method not supported");

        return new ResponseEntity<>(responseObject, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> invalid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<AppError> errors = new ArrayList<>();
        ex.getAllErrors().forEach(err -> {
            AppError error = new AppError(AppError.ErrorCode.DATA_INVALID,
                    err.getDefaultMessage());
            errors.add(error);
        });
        ResponseObject response = new ResponseObject();
        response.setMessage("Data is invalid.");
        response.setStatus(false);
        response.setErrors(errors);
        return new ResponseEntity<ResponseObject>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodNotValidException.class)
    public ResponseEntity<?> handleConstraintViolationException(MethodNotValidException ex,
            HttpServletRequest request) {

        AppError error = new AppError(AppError.ErrorCode.DATA_INVALID,
                ex.getMessage());
        ResponseObject<Void> responseObject = new ResponseObject<>();
        responseObject.setStatus(false);
        responseObject.setError(error);
        responseObject.setMessage("Validation failed");

        return new ResponseEntity<>(responseObject, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = { UnknownException.class })
    public ResponseEntity<?> unknown(Exception ex, HttpServletRequest request) {
        ResponseObject response = new ResponseObject();
        response.setStatus(false);
        response.setMessage("Something went wrong!.");
        response.setError(new AppError(ErrorCode.UNKNOWN, ex.getMessage()));
        return new ResponseEntity<ResponseObject>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
