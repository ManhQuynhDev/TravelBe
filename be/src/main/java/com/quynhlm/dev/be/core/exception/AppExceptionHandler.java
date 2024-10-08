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

    @ExceptionHandler(value = { UserAccountExistingException.class, UserAccountNotFoundException.class })
    public ResponseEntity<?> isExist(UserAccountExistingException ex, HttpServletRequest request) {
        ResponseObject response = new ResponseObject();
        response.setMessage("Data is invalid.");
        response.setError(new AppError(AppError.ErrorCode.ACCOUNT_EXIST, ex.getMessage()));
        return new ResponseEntity<ResponseObject>(response, HttpStatus.BAD_REQUEST);
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
