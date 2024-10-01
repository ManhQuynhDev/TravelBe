package com.quynhlm.dev.be.core;

import java.util.List;

import org.springframework.validation.ObjectError;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
@JsonInclude(Include.NON_NULL)

public class ResponseObject<T> {
    private String message;
    private List<ObjectError> errors;
    private AppError error;
    private T data;
}
