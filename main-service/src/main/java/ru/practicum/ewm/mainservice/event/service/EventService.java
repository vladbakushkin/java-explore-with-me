package ru.practicum.ewm.mainservice.event.service;

import ru.practicum.ewm.mainservice.event.dto.*;
import ru.practicum.ewm.mainservice.participationrequest.dto.ParticipationRequestDto;

import java.util.List;

public interface EventService {
    List<EventFullDto> getEvents(EventQueryParamsAdmin eventQueryParamsAdmin, Integer from, Integer size);

    List<EventFullDto> getEvents(Long userId, Integer from, Integer size);

    List<EventFullDto> getEvents(EventQueryParamsPublic eventQueryParamsPublic, Integer from, Integer size);

    EventFullDto getEvent(Long userId, Long eventId);

    EventFullDto getEvent(Long id);

    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest statusUpdateRequest);
}
