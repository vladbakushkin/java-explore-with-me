package ru.practicum.ewm.statsservice.statsdto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.ewm.statsservice.statsdto.constants.Constants;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HitDto {

    private String app;

    private String uri;

    private String ip;

    @JsonFormat(pattern = Constants.DATE_FORMAT_PATTERN)
    private LocalDateTime requestTime;
}
