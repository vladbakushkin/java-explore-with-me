package ru.practicum.ewm.statsservice.statsserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.statsservice.statsdto.HitDto;
import ru.practicum.ewm.statsservice.statsdto.StatsDto;
import ru.practicum.ewm.statsservice.statsserver.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.statsservice.statsdto.constants.Constants.DATE_FORMAT_PATTERN;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public HitDto saveStats(@RequestBody HitDto hitDto) {
        log.info("*-----SAVE STATS: {}-----*", hitDto);
        return statsService.saveStats(hitDto);
    }

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<StatsDto> getStats(@RequestParam @DateTimeFormat(pattern = DATE_FORMAT_PATTERN) LocalDateTime start,
                                   @RequestParam @DateTimeFormat(pattern = DATE_FORMAT_PATTERN) LocalDateTime end,
                                   @RequestParam(required = false) List<String> uris,
                                   @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("*-----GET PARAMS: {}, {}, {}, {}-----*", start, end, uris, unique);
        return statsService.getStats(start, end, uris, unique);
    }
}
