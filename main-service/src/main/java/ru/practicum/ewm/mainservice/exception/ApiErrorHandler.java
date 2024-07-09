package ru.practicum.ewm.mainservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Objects;

@RestControllerAdvice
public class ApiErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        return new ApiError(
                null,
                Objects.requireNonNull(e.getFieldError()).getDefaultMessage(),
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST.toString(),
                LocalDateTime.now().toString());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        return new ApiError(
                null,
                e.getMessage(),
                "Not found",
                HttpStatus.NOT_FOUND.toString(),
                LocalDateTime.now().toString());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(final BadRequestException e) {
        return new ApiError(
                null,
                e.getMessage(),
                "Bad ParticipationRequest",
                HttpStatus.BAD_REQUEST.toString(),
                LocalDateTime.now().toString());
    }
}
