package ru.practicum.ewm.mainservice.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.statsservice.statsdto.constants.Constants;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentQueryParams {
    private String text;

    private Long event;

    private Long author;

    @DateTimeFormat(pattern = Constants.DATE_FORMAT_PATTERN)
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = Constants.DATE_FORMAT_PATTERN)
    LocalDateTime rangeEnd;

    String sort = "ASC";
}
