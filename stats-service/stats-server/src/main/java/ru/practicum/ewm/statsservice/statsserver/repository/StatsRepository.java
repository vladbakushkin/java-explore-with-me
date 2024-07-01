package ru.practicum.ewm.statsservice.statsserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.statsservice.statsdto.StatsDto;
import ru.practicum.ewm.statsservice.statsserver.model.Stats;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Stats, Long> {

    @Query("SELECT NEW ru.practicum.ewm.statsservice.statsdto.StatsDto(s.app, s.uri, COUNT(s.ip)) " +
            "FROM Stats s " +
            "WHERE (s.requestTime BETWEEN :start AND :end) " +
            "GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(s.ip) DESC")
    List<StatsDto> findAllByTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT NEW ru.practicum.ewm.statsservice.statsdto.StatsDto(s.app, s.uri, COUNT(DISTINCT s.ip)) " +
            "FROM Stats s " +
            "WHERE (s.requestTime BETWEEN :start AND :end) " +
            "GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(DISTINCT s.ip) DESC")
    List<StatsDto> findAllDistinctIpAndTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT NEW ru.practicum.ewm.statsservice.statsdto.StatsDto(s.app, s.uri, COUNT(s.ip)) " +
            "FROM Stats s " +
            "WHERE (s.requestTime BETWEEN :start AND :end) AND s.uri IN (:uris) " +
            "GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(s.ip) DESC")
    List<StatsDto> findAllByTimeBetweenAndUriIn(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT NEW ru.practicum.ewm.statsservice.statsdto.StatsDto(s.app, s.uri, COUNT(DISTINCT s.ip)) " +
            "FROM Stats s " +
            "WHERE (s.requestTime BETWEEN :start AND :end) AND s.uri IN (:uris) " +
            "GROUP BY s.app, s.uri " +
            "ORDER BY COUNT(DISTINCT s.ip) DESC")
    List<StatsDto> findAllDistinctIpAndTimeBetweenAndUriIn(LocalDateTime start, LocalDateTime end, List<String> uris);
}
