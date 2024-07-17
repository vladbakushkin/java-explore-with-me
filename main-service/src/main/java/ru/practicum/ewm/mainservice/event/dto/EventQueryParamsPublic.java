package ru.practicum.ewm.mainservice.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.statsservice.statsdto.constants.Constants;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventQueryParamsPublic {
    private String text;

    private List<Long> categories;

    private Boolean paid;

    @DateTimeFormat(pattern = Constants.DATE_FORMAT_PATTERN)
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = Constants.DATE_FORMAT_PATTERN)
    LocalDateTime rangeEnd;

    Boolean onlyAvailable;

    String sort;
}
