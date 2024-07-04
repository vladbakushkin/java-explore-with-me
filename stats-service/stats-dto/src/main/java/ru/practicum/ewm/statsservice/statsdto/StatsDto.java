package ru.practicum.ewm.statsservice.statsdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatsDto {

    private String app;

    private String uri;

    private Long hits;
}
