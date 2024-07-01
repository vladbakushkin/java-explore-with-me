package ru.practicum.ewm.statsservice.statsserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.statsservice.statsdto.HitDto;
import ru.practicum.ewm.statsservice.statsdto.StatsDto;
import ru.practicum.ewm.statsservice.statsserver.exception.BadRequestException;
import ru.practicum.ewm.statsservice.statsserver.mapper.StatsMapper;
import ru.practicum.ewm.statsservice.statsserver.model.Stats;
import ru.practicum.ewm.statsservice.statsserver.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    @Override
    @Transactional
    public HitDto saveStats(HitDto hitDto) {
        hitDto.setRequestTime(LocalDateTime.now());
        Stats stats = statsMapper.toStats(hitDto);
        Stats savedStats = statsRepository.save(stats);
        return statsMapper.toHitDto(savedStats);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isEqual(end) || start.isAfter(end)) {
            throw new BadRequestException("Start time must be earlier than end time");
        }
        if (uris == null || uris.isEmpty()) {
            return unique ?
                    statsRepository.findAllDistinctIpAndTimeBetween(start, end) :
                    statsRepository.findAllByTimeBetween(start, end);
        }
        return unique ?
                statsRepository.findAllDistinctIpAndTimeBetweenAndUriIn(start, end, uris) :
                statsRepository.findAllByTimeBetweenAndUriIn(start, end, uris);
    }
}
