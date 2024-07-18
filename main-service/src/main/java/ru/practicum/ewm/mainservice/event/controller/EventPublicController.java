package ru.practicum.ewm.mainservice.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.mainservice.event.dto.EventFullDto;
import ru.practicum.ewm.mainservice.event.dto.EventQueryParamsPublic;
import ru.practicum.ewm.mainservice.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventPublicController {
    private final EventService eventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getEvents(EventQueryParamsPublic eventQueryParamsPublic,
                                        HttpServletRequest request,
                                        @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                        @RequestParam(defaultValue = "10") @Positive Integer size) {
        String clientIp = request.getRemoteAddr();
        String endpointPath = request.getRequestURI();
        log.info("*----- public getting events with params: {}, {}, {}, {}, {}", eventQueryParamsPublic, from, size,
                clientIp, endpointPath);
        return eventService.getEvents(eventQueryParamsPublic, from, size, clientIp, endpointPath);
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        String endpointPath = request.getRequestURI();
        log.info("*----- public getting event with id {}, {}, {}", id, clientIp, endpointPath);
        return eventService.getEvent(id, clientIp, endpointPath);
    }
}
