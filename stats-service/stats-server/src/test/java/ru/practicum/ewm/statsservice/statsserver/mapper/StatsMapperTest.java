package ru.practicum.ewm.statsservice.statsserver.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.ewm.statsservice.statsdto.HitDto;
import ru.practicum.ewm.statsservice.statsdto.StatsDto;
import ru.practicum.ewm.statsservice.statsserver.model.Stats;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatsMapperTest {
    private final StatsMapper statsMapper = Mappers.getMapper(StatsMapper.class);

    private final LocalDateTime now = LocalDateTime.now();

    @Test
    void toStats() {
        // given
        HitDto hitDto = makeHitDto();

        // when
        Stats result = statsMapper.toStats(hitDto);

        // then
        assertEquals("app", result.getApp());
        assertEquals("/events", result.getUri());
        assertEquals("127.0.0.1", result.getIp());
        assertEquals(now, result.getRequestTime());
    }

    @Test
    void toHitDto() {
        //given
        Stats stats = makeStats();

        // when
        HitDto result = statsMapper.toHitDto(stats);

        // then
        assertEquals("app", result.getApp());
        assertEquals("/events", result.getUri());
        assertEquals("127.0.0.1", result.getIp());
        assertEquals(now, result.getRequestTime());
    }

    @Test
    void toStatsDto() {
        // given
        Stats stats = makeStats();

        // when
        StatsDto result = statsMapper.toStatsDto(stats);

        // then
        assertEquals("app", result.getApp());
        assertEquals("/events", result.getUri());
    }

    private HitDto makeHitDto() {
        return HitDto.builder()
                .app("app")
                .ip("127.0.0.1")
                .uri("/events")
                .requestTime(now)
                .build();
    }

    private Stats makeStats() {
        return Stats.builder()
                .id(1L)
                .app("app")
                .ip("127.0.0.1")
                .uri("/events")
                .requestTime(now)
                .build();
    }
}