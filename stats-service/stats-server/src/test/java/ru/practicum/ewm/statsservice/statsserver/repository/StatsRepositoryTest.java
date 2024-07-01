package ru.practicum.ewm.statsservice.statsserver.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.ewm.statsservice.statsdto.StatsDto;
import ru.practicum.ewm.statsservice.statsdto.constants.Constants;
import ru.practicum.ewm.statsservice.statsserver.model.Stats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class StatsRepositoryTest {

    @Autowired
    private StatsRepository statsRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_PATTERN);
    private final String start = "2020-05-05 00:00:00";
    private final String end = "2035-05-05 00:00:00";
    private final LocalDateTime startTime = LocalDateTime.parse(start, formatter);
    private final LocalDateTime endTime = LocalDateTime.parse(end, formatter);

    @Test
    void findAllByTimeBetween() {
        Stats stats1 = Stats.builder()
                .app("app")
                .ip("127.0.0.1")
                .uri("/events")
                .requestTime(LocalDateTime.now())
                .build();

        Stats stats2 = Stats.builder()
                .app("app")
                .ip("127.0.0.1")
                .uri("/events")
                .requestTime(LocalDateTime.now())
                .build();

        StatsDto statsDto = StatsDto.builder()
                .app("app")
                .uri("/events")
                .hits(2L)
                .build();

        statsRepository.save(stats1);
        statsRepository.save(stats2);

        List<StatsDto> result = statsRepository.findAllByTimeBetween(startTime, endTime);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(statsDto.getApp(), result.get(0).getApp());
        assertEquals(statsDto.getUri(), result.get(0).getUri());
        assertEquals(statsDto.getHits(), result.get(0).getHits());
    }

    @Test
    void findAllDistinctIpAndTimeBetween() {
        Stats stats1 = Stats.builder()
                .app("app")
                .ip("127.0.0.1")
                .uri("/events")
                .requestTime(LocalDateTime.now())
                .build();

        Stats stats2 = Stats.builder()
                .app("app")
                .ip("127.0.0.1")
                .uri("/events")
                .requestTime(LocalDateTime.now())
                .build();

        StatsDto statsDto = StatsDto.builder()
                .app("app")
                .uri("/events")
                .hits(1L)
                .build();

        statsRepository.save(stats1);
        statsRepository.save(stats2);

        List<StatsDto> result = statsRepository.findAllDistinctIpAndTimeBetween(startTime, endTime);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(statsDto.getApp(), result.get(0).getApp());
        assertEquals(statsDto.getUri(), result.get(0).getUri());
        assertEquals(statsDto.getHits(), result.get(0).getHits());
    }

    @Test
    void findAllByTimeBetweenAndUriIn() {
        Stats stats1 = Stats.builder()
                .app("app")
                .ip("127.0.0.1")
                .uri("/events")
                .requestTime(LocalDateTime.now())
                .build();

        Stats stats2 = Stats.builder()
                .app("app")
                .ip("127.0.0.1")
                .uri("/events")
                .requestTime(LocalDateTime.now())
                .build();

        StatsDto statsDto = StatsDto.builder()
                .app("app")
                .uri("/events")
                .hits(2L)
                .build();

        List<String> uris = List.of("/events");

        statsRepository.save(stats1);
        statsRepository.save(stats2);

        List<StatsDto> result = statsRepository.findAllByTimeBetweenAndUriIn(startTime, endTime, uris);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(statsDto.getApp(), result.get(0).getApp());
        assertEquals(statsDto.getUri(), result.get(0).getUri());
        assertEquals(statsDto.getHits(), result.get(0).getHits());
    }

    @Test
    void findAllDistinctIpAndTimeBetweenAndUriIn() {
        Stats stats1 = Stats.builder()
                .app("app")
                .ip("127.0.0.1")
                .uri("/events")
                .requestTime(LocalDateTime.now())
                .build();

        Stats stats2 = Stats.builder()
                .app("app")
                .ip("127.0.0.1")
                .uri("/events")
                .requestTime(LocalDateTime.now())
                .build();

        StatsDto statsDto = StatsDto.builder()
                .app("app")
                .uri("/events")
                .hits(1L)
                .build();

        List<String> uris = List.of("/events");

        statsRepository.save(stats1);
        statsRepository.save(stats2);

        List<StatsDto> result = statsRepository.findAllDistinctIpAndTimeBetweenAndUriIn(startTime, endTime, uris);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(statsDto.getApp(), result.get(0).getApp());
        assertEquals(statsDto.getUri(), result.get(0).getUri());
        assertEquals(statsDto.getHits(), result.get(0).getHits());
    }
}