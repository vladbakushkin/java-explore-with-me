package ru.practicum.ewm.statsservice.statsserver.service;

import ru.practicum.ewm.statsservice.statsdto.HitDto;
import ru.practicum.ewm.statsservice.statsdto.StatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    HitDto saveStats(HitDto hitDto);

    List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
