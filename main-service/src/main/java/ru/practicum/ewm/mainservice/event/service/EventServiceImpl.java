package ru.practicum.ewm.mainservice.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.mainservice.category.model.Category;
import ru.practicum.ewm.mainservice.category.repository.CategoryRepository;
import ru.practicum.ewm.mainservice.event.dto.*;
import ru.practicum.ewm.mainservice.event.mapper.EventMapper;
import ru.practicum.ewm.mainservice.event.model.Event;
import ru.practicum.ewm.mainservice.event.model.Location;
import ru.practicum.ewm.mainservice.event.model.QEvent;
import ru.practicum.ewm.mainservice.event.model.state.EventState;
import ru.practicum.ewm.mainservice.event.model.state.EventStateActionAdmin;
import ru.practicum.ewm.mainservice.event.model.state.EventStateActionUser;
import ru.practicum.ewm.mainservice.event.repository.EventRepository;
import ru.practicum.ewm.mainservice.exception.BadRequestException;
import ru.practicum.ewm.mainservice.exception.ConflictException;
import ru.practicum.ewm.mainservice.exception.NotFoundException;
import ru.practicum.ewm.mainservice.participationrequest.dto.ParticipationRequestDto;
import ru.practicum.ewm.mainservice.participationrequest.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.mainservice.participationrequest.model.ParticipationRequest;
import ru.practicum.ewm.mainservice.participationrequest.model.RequestStatus;
import ru.practicum.ewm.mainservice.participationrequest.repository.ParticipationRequestRepository;
import ru.practicum.ewm.mainservice.user.model.User;
import ru.practicum.ewm.mainservice.user.repository.UserRepository;
import ru.practicum.ewm.statsservice.statsclient.StatsClient;
import ru.practicum.ewm.statsservice.statsdto.HitDto;
import ru.practicum.ewm.statsservice.statsdto.StatsDto;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private final JPAQueryFactory queryFactory;
    private final ParticipationRequestRepository participationRequestRepository;
    private final ParticipationRequestMapper participationRequestMapper;

    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEvents(EventQueryParamsAdmin query, Integer from, Integer size) {
        QEvent event = QEvent.event;

        BooleanBuilder params = makeQuery(event, query.getUsers(), query.getStates(), query.getCategories(),
                query.getRangeStart(), query.getRangeEnd(), null, null, null);

        List<Event> events = queryFactory.selectFrom(event)
                .where(params)
                .offset(from)
                .limit(size)
                .fetch();

        List<EventFullDto> eventFullDtoList = eventMapper.toEventFullDtoList(events);

        Map<Long, Long> eventViews = getEventViews(events);
        List<EventFullDto> eventFullDtoListWithViews = eventFullDtoList.stream()
                .peek(eventFullDto -> eventFullDto.setViews(eventViews.get(eventFullDto.getId())))
                .collect(Collectors.toList());
        log.info("#----- admin get events: {}", eventFullDtoListWithViews);
        return eventFullDtoListWithViews;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEvents(Long userId, Integer from, Integer size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);

        List<EventFullDto> eventFullDtoList = eventMapper.toEventFullDtoList(events);
        log.info("#----- private get events: {}", eventFullDtoList);
        return eventFullDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEvents(EventQueryParamsPublic query, Integer from, Integer size,
                                        String clientIp, String endpointPath) {
        QEvent event = QEvent.event;

        // в публичном эндпоинте только опубликованные события
        BooleanBuilder params = makeQuery(event, null, List.of("PUBLISHED"), query.getCategories(),
                query.getRangeStart(), query.getRangeEnd(), query.getText(), query.getPaid(), query.getOnlyAvailable());

        List<Event> events = queryFactory.selectFrom(event)
                .where(params)
                .orderBy(event.eventDate.asc())
                .offset(from)
                .limit(size)
                .fetch();

        saveStats(endpointPath, clientIp);

        List<EventFullDto> eventFullDtoList = eventMapper.toEventFullDtoList(events);

        Map<Long, Long> eventViews = getEventViews(events);
        List<EventFullDto> eventFullDtoListWithViews = eventFullDtoList.stream()
                .peek(eventFullDto -> eventFullDto.setViews(eventViews.get(eventFullDto.getId())))
                .collect(Collectors.toList());

        if (query.getSort().equals("VIEWS")) {
            Comparator<EventFullDto> byViews = Comparator.comparing(EventFullDto::getViews).reversed();
            eventFullDtoListWithViews = eventFullDtoListWithViews.stream()
                    .sorted(byViews)
                    .collect(Collectors.toList());
        }

        log.info("#----- public get events: {}", eventFullDtoListWithViews);
        return eventFullDtoListWithViews;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long id, String clientIp, String endpointPath) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id = " + id + " not found"));
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id = " + id + " is not published");
        }
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);

        saveStats(endpointPath, clientIp);

        Map<Long, Long> eventViews = getEventViews(List.of(event));
        eventFullDto.setViews(eventViews.get(event.getId()));

        log.info("#----- get public eventFullDto: {}", eventFullDto);
        return eventFullDto;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id = " + userId + " not found");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id = " + eventId + " not found"));

        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        log.info("#----- public get event: {}", eventFullDto);
        return eventFullDto;
    }

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        Event event = eventMapper.toEvent(newEventDto);

        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id = " + userId + " not found"));
        event.setInitiator(initiator);

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id = " + newEventDto.getCategory() + " not found"));
        event.setCategory(category);

        event.setState(EventState.PENDING);

        event.setConfirmedRequests(0);

        event.setLat(newEventDto.getLocation().getLat());
        event.setLon(newEventDto.getLocation().getLon());

        Event savedEvent = eventRepository.save(event);

        EventFullDto eventFullDto = eventMapper.toEventFullDto(savedEvent);
        log.info("#----- added eventFullDto: {}", eventFullDto);

        return eventFullDto;
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id = " + userId + " not found");
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id = " + eventId + " not found"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        event = update(event, request.getAnnotation(), request.getCategory(), request.getDescription(), request.getLocation(),
                request.getPaid(), request.getParticipantLimit(), request.getRequestModeration(), request.getTitle());

        if (request.getEventDate() != null) {
            if (event.getPublishedOn() != null &&
                    request.getEventDate().isBefore(LocalDateTime.now())) {
                throw new ConflictException("Can't be earlier than two hours from the current moment.");
            }
            event.setEventDate(request.getEventDate());
        }

        event.setState(request.getStateAction() == EventStateActionUser.SEND_TO_REVIEW ?
                EventState.PENDING : EventState.CANCELED);

        Event savedEvent = eventRepository.save(event);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(savedEvent);
        log.info("#----- user updated event: {}", eventFullDto);
        return eventFullDto;
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id = " + eventId + " not found"));

        event = update(event, request.getAnnotation(), request.getCategory(), request.getDescription(), request.getLocation(),
                request.getPaid(), request.getParticipantLimit(), request.getRequestModeration(), request.getTitle());

        if (request.getEventDate() != null) {
            if (event.getPublishedOn() != null &&
                    request.getEventDate().isBefore(event.getPublishedOn().minusHours(1))) {
                throw new ConflictException("Event published on " + event.getPublishedOn() + " is after now");
            }
            event.setEventDate(request.getEventDate());
        }

        if (request.getStateAction() != null) {
            if (request.getStateAction() == EventStateActionAdmin.PUBLISH_EVENT &&
                    event.getState() == EventState.PENDING) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (request.getStateAction() == EventStateActionAdmin.REJECT_EVENT
                    && event.getState() != EventState.PUBLISHED) {
                event.setState(EventState.CANCELED);
            } else {
                throw new ConflictException("Cannot publish the event because it's not in the right state: PUBLISHED");
            }
        }

        Event savedEvent = eventRepository.save(event);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(savedEvent);

        log.info("#----- admin updated event: {}", eventFullDto);
        return eventFullDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id = " + userId + " not found");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id = " + eventId + " not found"));

        List<ParticipationRequest> eventParticipants = participationRequestRepository.findAllByEvent(event);

        List<ParticipationRequestDto> participationRequestDtoList = participationRequestMapper.toParticipationRequestDtoList(eventParticipants);
        log.info("#----- private get event participants : {}", participationRequestDtoList);
        return participationRequestDtoList;
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest statusUpdateRequest) {
        userRepository.existsById(userId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id = " + eventId + " not found"));

        List<Long> requestIds = statusUpdateRequest.getRequestIds();
        List<ParticipationRequest> requests = participationRequestRepository.findAllById(requestIds);

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new ConflictException("Confirmation is not required");
        }

        int participantCount = participationRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 &&
                event.getParticipantLimit() <= participantCount) {
            throw new ConflictException("The participant limit has been reached");
        }

        List<ParticipationRequest> confirmedRequests = requests.stream()
                .peek(request -> {
                    if (statusUpdateRequest.getStatus() == RequestStatus.CONFIRMED) {
                        request.setStatus(RequestStatus.CONFIRMED);
                    }
                })
                .collect(Collectors.toList());

        List<ParticipationRequest> rejectedRequests = requests.stream()
                .peek(request -> {
                    if (statusUpdateRequest.getStatus() == RequestStatus.REJECTED) {
                        request.setStatus(RequestStatus.REJECTED);
                    }
                })
                .collect(Collectors.toList());

        participationRequestRepository.saveAll(confirmedRequests);
        participationRequestRepository.saveAll(rejectedRequests);

        event.setConfirmedRequests(confirmedRequests.size());
        eventRepository.save(event);

        EventRequestStatusUpdateResult eventRequestStatusUpdateResult = new EventRequestStatusUpdateResult();
        eventRequestStatusUpdateResult.setConfirmedRequests(
                participationRequestMapper.toParticipationRequestDtoList(confirmedRequests));
        eventRequestStatusUpdateResult.setRejectedRequests(
                participationRequestMapper.toParticipationRequestDtoList(rejectedRequests));

        log.info("#----- private requests updated: {}", eventRequestStatusUpdateResult);
        return eventRequestStatusUpdateResult;
    }

    private BooleanBuilder makeQuery(QEvent event, List<Long> users, List<String> states, List<Long> categories,
                                     LocalDateTime rangeStart, LocalDateTime rangeEnd, String text,
                                     Boolean paid, Boolean onlyAvailable) {
        BooleanBuilder params = new BooleanBuilder();

        if (text != null) {
            params.or(event.annotation.containsIgnoreCase(text));
            params.or(event.description.containsIgnoreCase(text));
        }

        if (categories != null && !categories.isEmpty()) {
            params.and(event.category.id.in(categories));
        }

        if (paid != null) {
            params.and((event.paid.eq(paid)));
        }

        if (rangeStart != null) {
            params.and(event.eventDate.after(rangeStart));
        }

        if (rangeEnd != null) {
            params.and(event.eventDate.before(rangeEnd));
        }

        if (rangeStart != null && rangeEnd != null) {
            if (rangeStart.isAfter(rangeEnd)) {
                throw new BadRequestException("End time must be after start time");
            }
            params.and(event.eventDate.between(rangeStart, rangeEnd));
        }

        if (onlyAvailable != null && onlyAvailable) {
            NumberExpression<Integer> participantAvailable = event.participantLimit.subtract(event.confirmedRequests);
            params.and(participantAvailable.goe(0));
        }

        if (users != null && users.isEmpty()) {
            params.and(event.initiator.id.in(users));
        }

        if (states != null && !states.isEmpty()) {
            List<EventState> eventStates = states.stream()
                    .map(EventState::valueOf).collect(Collectors.toList());
            params.and((event.state.in(eventStates)));
        }
        return params;
    }

    private Event update(Event eventToUpdate, String annotation, Long categoryId, String description,
                         Location location, Boolean paid, Integer participantLimit, Boolean requestModeration,
                         String title) {

        if (annotation != null) {
            eventToUpdate.setAnnotation(annotation);
        }
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category with id = " + categoryId + " not found"));
            eventToUpdate.setCategory(category);
        }
        if (description != null) {
            eventToUpdate.setDescription(description);
        }
        if (location != null) {
            eventToUpdate.setLon(location.getLon());
            eventToUpdate.setLat(location.getLat());
        }
        if (paid != null) {
            eventToUpdate.setPaid(paid);
        }
        if (participantLimit != null) {
            eventToUpdate.setParticipantLimit(participantLimit);
        }
        if (requestModeration != null) {
            eventToUpdate.setRequestModeration(requestModeration);
        }
        if (title != null) {
            eventToUpdate.setTitle(title);
        }
        return eventToUpdate;
    }

    private void saveStats(String endpointPath, String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        String app = "ewm-main-service";
        HitDto hitDto = new HitDto(app, endpointPath, clientIp, now);
        statsClient.saveStats(hitDto);
    }

    private Map<Long, Long> getEventViews(List<Event> events) {
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        List<LocalDateTime> eventCreationsTime = events.stream()
                .map(Event::getCreatedOn)
                .sorted()
                .collect(Collectors.toList());

        LocalDateTime startTime = eventCreationsTime.get(0);
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);

        ResponseEntity<Object> response = statsClient.getStats(startTime, endTime, uris, true);

        List<StatsDto> statsDtoList = objectMapper.convertValue(response.getBody(), new TypeReference<>() {
        });

        return statsDtoList.stream()
                .collect(Collectors.toMap(
                        statsDto -> Long.parseLong(statsDto.getUri().substring("/events/".length())),
                        StatsDto::getHits
                ));
    }
}
