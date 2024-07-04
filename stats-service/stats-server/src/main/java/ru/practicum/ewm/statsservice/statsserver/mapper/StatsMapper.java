package ru.practicum.ewm.statsservice.statsserver.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.statsservice.statsdto.HitDto;
import ru.practicum.ewm.statsservice.statsdto.StatsDto;
import ru.practicum.ewm.statsservice.statsserver.model.Stats;

@Mapper(componentModel = "spring")
public interface StatsMapper {
    Stats toStats(HitDto hitDto);

    HitDto toHitDto(Stats stats);

    StatsDto toStatsDto(Stats stats);
}
