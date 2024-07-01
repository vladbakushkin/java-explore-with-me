package ru.practicum.ewm.statsservice.statsdto;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatsDto {

    private String app;

    private String uri;

    private Long hits;
}
