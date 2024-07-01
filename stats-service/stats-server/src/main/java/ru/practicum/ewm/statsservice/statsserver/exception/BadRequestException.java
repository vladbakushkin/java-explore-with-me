package ru.practicum.ewm.statsservice.statsserver.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
