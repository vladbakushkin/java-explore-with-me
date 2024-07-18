package ru.practicum.ewm.statsservice.statsserver.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

@RestControllerAdvice
public class ApiErrorHandler {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return new ApiError(
                Collections.singletonList(Arrays.toString(e.getStackTrace())),
                e.getMessage(),
                "Incorrect params. " +
                        "Name: \"" + Objects.requireNonNull(e.getParameterName()) + "\" " +
                        "Type: \"" + Objects.requireNonNull(e.getParameterType()) + "\"",
                HttpStatus.BAD_REQUEST.toString(),
                LocalDateTime.now().toString());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return new ApiError(
                Collections.singletonList(Arrays.toString(e.getStackTrace())),
                e.getMessage(),
                "Incorrect request. " +
                        "Http Input Message: \"" + Objects.requireNonNull(e.getMessage()) + "\" ",
                HttpStatus.BAD_REQUEST.toString(),
                LocalDateTime.now().toString());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConstraintViolationException(final ConstraintViolationException e) {
        return new ApiError(
                Collections.singletonList(Arrays.toString(e.getStackTrace())),
                e.getMessage(),
                e.getCause().toString(),
                HttpStatus.CONFLICT.toString(),
                LocalDateTime.now().toString());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(final BadRequestException e) {
        return new ApiError(
                Collections.singletonList(Arrays.toString(e.getStackTrace())),
                e.getMessage(),
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST.toString(),
                LocalDateTime.now().toString());
    }
}
