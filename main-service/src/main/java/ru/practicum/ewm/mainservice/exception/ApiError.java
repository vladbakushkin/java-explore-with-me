package ru.practicum.ewm.mainservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ApiError {
    private final List<String> errors;
    private final String message;
    private final String reason;
    private final String status;
    private final String timestamp;
}
