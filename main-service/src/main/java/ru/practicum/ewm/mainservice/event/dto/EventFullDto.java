package ru.practicum.ewm.mainservice.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.mainservice.category.dto.CategoryDto;
import ru.practicum.ewm.mainservice.comment.dto.CommentDto;
import ru.practicum.ewm.mainservice.event.model.Location;
import ru.practicum.ewm.mainservice.event.model.state.EventState;
import ru.practicum.ewm.mainservice.user.dto.UserShortDto;
import ru.practicum.ewm.statsservice.statsdto.constants.Constants;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {
    private Long id;

    private String annotation;

    private CategoryDto category;

    private String description;

    @JsonFormat(pattern = Constants.DATE_FORMAT_PATTERN)
    private LocalDateTime eventDate;

    private Location location;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration;

    private String title;

    private Long confirmedRequests;

    @JsonFormat(pattern = Constants.DATE_FORMAT_PATTERN)
    private LocalDateTime createdOn;

    private UserShortDto initiator;

    @JsonFormat(pattern = Constants.DATE_FORMAT_PATTERN)
    private LocalDateTime publishedOn;

    private EventState state;

    private Long views;

    private List<CommentDto> comments;
}
