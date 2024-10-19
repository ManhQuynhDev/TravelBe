package com.quynhlm.dev.be.core.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            LocationExistingException.class, StoryNotFoundException.class })
    public ResponseEntity<ResponseObject> handleCustomExceptions(RuntimeException ex, HttpServletRequest request) {
        ResponseObject response = new ResponseObject();
        response.setMessage("Data is invalid.");

        AppError.ErrorCode errorCode;
        if (ex instanceof UserAccountExistingException || ex instanceof UserAccountNotFoundException) {
            errorCode = AppError.ErrorCode.ACCOUNT_EXIST;
        } else if (ex instanceof LocationExistingException) {
            errorCode = AppError.ErrorCode.LOCATION_EXIST;
        } else if (ex instanceof StoryNotFoundException) {
            errorCode = AppError.ErrorCode.STORY_EXIST;
        } else {
            errorCode = AppError.ErrorCode.UNKNOW;
        }

        response.setError(new AppError(errorCode, ex.getMessage()));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
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
        response.setErrors(errors);
        return new ResponseEntity<ResponseObject>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = { UnknowException.class })
    public ResponseEntity<?> unknow(Exception ex, HttpServletRequest request) {
        ResponseObject response = new ResponseObject();
        response.setMessage("Something went wrong!.");
        response.setError(new AppError(ErrorCode.UNKNOW, ex.getMessage()));
        return new ResponseEntity<ResponseObject>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
