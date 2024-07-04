package ru.practicum.ewm.statsservice.statsserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.statsservice.statsdto.HitDto;
import ru.practicum.ewm.statsservice.statsdto.StatsDto;
import ru.practicum.ewm.statsservice.statsdto.constants.Constants;
import ru.practicum.ewm.statsservice.statsserver.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
@AutoConfigureMockMvc
class StatsControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatsService statsService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_PATTERN);
    private final String start = "2020-05-05 00:00:00";
    private final String end = "2035-05-05 00:00:00";
    private final LocalDateTime startTime = LocalDateTime.parse(start, formatter);
    private final LocalDateTime endTime = LocalDateTime.parse(end, formatter);

    @Test
    @SneakyThrows
    void saveStats_Valid_ReturnsHitDto() {
        // given
        HitDto requestHitDto = HitDto.builder()
                .app("app")
                .ip("127.0.0.1")
                .uri("/events")
                .build();

        HitDto responseHitDto = HitDto.builder()
                .app("app")
                .ip("127.0.0.1")
                .uri("/events")
                .requestTime(LocalDateTime.now())
                .build();

        when(statsService.saveStats(requestHitDto))
                .thenReturn(responseHitDto);

        // then
        mockMvc.perform(
                        post("/hit")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestHitDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.app").value(responseHitDto.getApp()))
                .andExpect(jsonPath("$.ip").value(responseHitDto.getIp()))
                .andExpect(jsonPath("$.uri").value(responseHitDto.getUri()))
                .andExpect(jsonPath("$.requestTime").isNotEmpty());
    }

    @Test
    @SneakyThrows
    void getStats_Valid_ReturnsListOfStatsDto() {
        // given
        StatsDto statsDto = StatsDto.builder()
                .app("app")
                .uri("/events")
                .hits(1L)
                .build();

        when(statsService.getStats(startTime, endTime, null, false))
                .thenReturn(List.of(statsDto));

        // then
        mockMvc.perform(
                        get("/stats")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("start", start)
                                .param("end", end))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value(statsDto.getApp()))
                .andExpect(jsonPath("$[0].uri").value(statsDto.getUri()))
                .andExpect(jsonPath("$[0].hits").value(statsDto.getHits()));
    }
}