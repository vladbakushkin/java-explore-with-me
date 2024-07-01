package ru.practicum.ewm.statsservice.statsserver.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.statsservice.statsdto.HitDto;
import ru.practicum.ewm.statsservice.statsdto.StatsDto;
import ru.practicum.ewm.statsservice.statsdto.constants.Constants;
import ru.practicum.ewm.statsservice.statsserver.exception.BadRequestException;
import ru.practicum.ewm.statsservice.statsserver.mapper.StatsMapper;
import ru.practicum.ewm.statsservice.statsserver.model.Stats;
import ru.practicum.ewm.statsservice.statsserver.repository.StatsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {

    @Mock
    private StatsRepository statsRepository;

    @Mock
    private StatsMapper statsMapper;

    @InjectMocks
    private StatsServiceImpl statsService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_PATTERN);
    private final String start = "2020-05-05 00:00:00";
    private final String end = "2035-05-05 00:00:00";
    private final LocalDateTime startTime = LocalDateTime.parse(start, formatter);
    private final LocalDateTime endTime = LocalDateTime.parse(end, formatter);

    @Test
    void saveStats_Valid_ReturnsHitDto() {
        // given
        HitDto requestHitDto = makeRequestHitDto();

        Stats stats = makeStats();

        HitDto responseHitDto = makeResponseHitDto();

        when(statsMapper.toStats(requestHitDto)).thenReturn(stats);
        when(statsMapper.toHitDto(stats)).thenReturn(responseHitDto);
        when(statsRepository.save(stats)).thenReturn(stats);

        // when
        HitDto savedStats = statsService.saveStats(requestHitDto);

        // then
        assertEquals(requestHitDto.getApp(), savedStats.getApp());
        assertEquals(requestHitDto.getIp(), savedStats.getIp());
        assertEquals(requestHitDto.getUri(), savedStats.getUri());
        assertNotNull(savedStats.getRequestTime());
        verify(statsRepository, times(1)).save(any(Stats.class));
    }

    @Test
    void getStats_UrisNotNullAndUniqueFalse_ReturnsListOfStatsDto() {
        // given
        List<String> uris = List.of("/events");
        StatsDto statsDto = makeStatsDto();
        when(statsRepository.findAllByTimeBetweenAndUriIn(startTime, endTime, uris))
                .thenReturn(List.of(statsDto));

        // when
        List<StatsDto> result = statsService.getStats(startTime, endTime, uris, false);

        // then
        assertNotNull(result);
        assertEquals(statsDto.getApp(), result.get(0).getApp());
        assertEquals(statsDto.getUri(), result.get(0).getUri());
        assertEquals(statsDto.getHits(), result.get(0).getHits());
    }

    @Test
    void getStats_UrisNotNullAndUniqueTrue_ReturnsListOfStatsDto() {
        // given
        List<String> uris = List.of("/events");
        StatsDto statsDto = makeStatsDto();
        when(statsRepository.findAllDistinctIpAndTimeBetweenAndUriIn(startTime, endTime, uris))
                .thenReturn(List.of(statsDto));

        // when
        List<StatsDto> result = statsService.getStats(startTime, endTime, uris, true);

        // then
        assertNotNull(result);
        assertEquals(statsDto.getApp(), result.get(0).getApp());
        assertEquals(statsDto.getUri(), result.get(0).getUri());
        assertEquals(statsDto.getHits(), result.get(0).getHits());
    }

    @Test
    void getStats_UrisNullAndUniqueFalse_ReturnsListOfStatsDto() {
        // given
        StatsDto statsDto = makeStatsDto();
        when(statsRepository.findAllByTimeBetween(startTime, endTime))
                .thenReturn(List.of(statsDto));

        // when
        List<StatsDto> result = statsService.getStats(startTime, endTime, null, false);

        // then
        assertNotNull(result);
        assertEquals(statsDto.getApp(), result.get(0).getApp());
        assertEquals(statsDto.getUri(), result.get(0).getUri());
        assertEquals(statsDto.getHits(), result.get(0).getHits());
    }

    @Test
    void getStats_UrisNullAndUniqueTrue_ReturnsListOfStatsDto() {
        // given
        StatsDto statsDto = makeStatsDto();
        when(statsRepository.findAllDistinctIpAndTimeBetween(startTime, endTime))
                .thenReturn(List.of(statsDto));

        // when
        List<StatsDto> result = statsService.getStats(startTime, endTime, null, true);

        // then
        assertNotNull(result);
        assertEquals(statsDto.getApp(), result.get(0).getApp());
        assertEquals(statsDto.getUri(), result.get(0).getUri());
        assertEquals(statsDto.getHits(), result.get(0).getHits());
    }

    @Test
    void getStats_StartEqualsEnd_ThrowsBadRequestException() {
        // given
        // when
        // then
        assertThrows(BadRequestException.class,
                () -> statsService.getStats(startTime, startTime, null, false));
    }

    @Test
    void getStats_StartAfterEnd_ReturnsListOfStatsDto() {
        // given
        // when
        // then
        assertThrows(BadRequestException.class,
                () -> statsService.getStats(endTime, startTime, null, false));
    }

    private HitDto makeRequestHitDto() {
        return HitDto.builder()
                .app("app")
                .ip("127.0.0.1")
                .uri("/events")
                .build();
    }

    private Stats makeStats() {
        return Stats.builder()
                .id(1L)
                .app("app")
                .ip("127.0.0.1")
                .uri("/events")
                .requestTime(LocalDateTime.now())
                .build();
    }

    private HitDto makeResponseHitDto() {
        return HitDto.builder()
                .app("app")
                .ip("127.0.0.1")
                .uri("/events")
                .requestTime(LocalDateTime.now())
                .build();
    }

    private StatsDto makeStatsDto() {
        return StatsDto.builder()
                .app("app")
                .uri("/events")
                .hits(1L)
                .build();
    }
}