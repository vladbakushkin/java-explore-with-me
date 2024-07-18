package ru.practicum.ewm.mainservice.event.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.mainservice.category.model.Category;
import ru.practicum.ewm.mainservice.category.repository.CategoryRepository;
import ru.practicum.ewm.mainservice.event.dto.*;
import ru.practicum.ewm.mainservice.event.mapper.EventMapper;
import ru.practicum.ewm.mainservice.event.model.Event;
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

import java.time.LocalDateTime;
import java.util.List;
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

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEvents(EventQueryParamsAdmin eventQueryParamsAdmin, Integer from, Integer size) {
        QEvent event = QEvent.event;
        BooleanBuilder params = new BooleanBuilder();

        List<Long> users = eventQueryParamsAdmin.getUsers();
        if (users != null && users.isEmpty()) {
            params.and(event.initiator.id.in(users));
        }

        List<String> states = eventQueryParamsAdmin.getStates();
        if (states != null && !states.isEmpty()) {
            List<EventState> eventStates = states.stream()
                    .map(EventState::valueOf).collect(Collectors.toList());
            params.and((event.state.in(eventStates)));
        }

        List<Long> categories = eventQueryParamsAdmin.getCategories();
        if (categories != null && !categories.isEmpty()) {
            params.and(event.category.id.in(categories));
        }

        LocalDateTime rangeStart = eventQueryParamsAdmin.getRangeStart();
        if (rangeStart != null) {
            params.and(event.eventDate.after(rangeStart));
        }

        LocalDateTime rangeEnd = eventQueryParamsAdmin.getRangeEnd();
        if (rangeEnd != null) {
            params.and(event.eventDate.before(rangeEnd));
        }

        if (rangeStart != null && rangeEnd != null) {
            params.and(event.eventDate.between(rangeStart, rangeEnd));
        }

        List<Event> events = queryFactory.selectFrom(event)
                .where(params)
                .offset(from)
                .limit(size)
                .fetch();

        List<EventFullDto> eventFullDtoList = eventMapper.toEventFullDtoList(events);

        // TODO добавление views к dto
        List<EventFullDto> eventFullDtos = makeEventDtoListWithViews(eventFullDtoList);
        log.info("#----- admin get events: {}", eventFullDtos);
        return eventFullDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEvents(Long userId, Integer from, Integer size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);

        List<EventFullDto> eventFullDtoList = eventMapper.toEventFullDtoList(events);
        log.info("#----- ???private get events: {}", eventFullDtoList);
        return eventFullDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEvents(EventQueryParamsPublic eventQueryParamsPublic, Integer from, Integer size) {
        QEvent event = QEvent.event;
        BooleanBuilder params = new BooleanBuilder();

        // в публичном эндпоинте только опубликованные события
        params.and(event.state.eq(EventState.PUBLISHED));

        String text = eventQueryParamsPublic.getText();
        if (text != null) {
            params.and(event.annotation.containsIgnoreCase(text));
            params.or(event.description.containsIgnoreCase(text));
        }

        List<Long> categories = eventQueryParamsPublic.getCategories();
        if (categories != null && !categories.isEmpty()) {
            params.and(event.category.id.in(categories));
        }

        Boolean paid = eventQueryParamsPublic.getPaid();
        if (paid != null) {
            params.and((event.paid.eq(paid)));
        }

        LocalDateTime rangeStart = eventQueryParamsPublic.getRangeStart();
        if (rangeStart != null) {
            params.and(event.eventDate.after(rangeStart));
        }

        LocalDateTime rangeEnd = eventQueryParamsPublic.getRangeEnd();
        if (rangeEnd != null) {
            params.and(event.eventDate.before(rangeEnd));
        }

        if (rangeStart != null && rangeEnd != null) {
            if (rangeStart.isAfter(rangeEnd)) {
                throw new BadRequestException("End time must be after start time");
            }
            params.and(event.eventDate.between(rangeStart, rangeEnd));
        }

        if (eventQueryParamsPublic.getOnlyAvailable() != null && eventQueryParamsPublic.getOnlyAvailable()) {
            NumberExpression<Integer> participantAvailable = event.participantLimit.subtract(event.confirmedRequests);
            params.and(participantAvailable.goe(0));
        }

        OrderSpecifier<?> sort;
        if (eventQueryParamsPublic.getSort() != null && eventQueryParamsPublic.getSort().equals("EVENT_DATE")) {
            sort = event.eventDate.asc();
        } else if (eventQueryParamsPublic.getSort() != null && eventQueryParamsPublic.getSort().equals("VIEWS")) {
            // TODO добавить сортировку по views
            sort = event.eventDate.desc();
        } else {
            sort = event.id.asc();
        }
        List<Event> events = queryFactory.selectFrom(event)
                .where(params)
                .orderBy(sort)
                .offset(from)
                .limit(size)
                .fetch();

        List<EventFullDto> eventFullDtoList = eventMapper.toEventFullDtoList(events);
        log.info("#----- public get events: {}", eventFullDtoList);
        return eventFullDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id = " + id + " not found"));
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id = " + id + " is not published");
        }
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        log.info("#----- get public eventFullDto: {}", eventFullDto);
        // TODO: информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики
        eventFullDto.setViews(1L);
        return eventFullDto;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id = " + userId + " not found"));

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
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id = " + userId + " not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id = " + eventId + " not found"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getCategory() != null) {
            Long categoryId = updateEventUserRequest.getCategory();
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category with id = " + categoryId + " not found"));
            event.setCategory(category);
        }
        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getLocation() != null) {
            event.setLon(updateEventUserRequest.getLocation().getLon());
            event.setLat(updateEventUserRequest.getLocation().getLat());
        }
        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        // TODO тернарный оператор
        if (updateEventUserRequest.getStateAction() == EventStateActionUser.SEND_TO_REVIEW) {
            event.setState(EventState.PENDING);
        }
        if (updateEventUserRequest.getStateAction() == EventStateActionUser.CANCEL_REVIEW) {
            event.setState(EventState.CANCELED);
        }
        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }
        Event savedEvent = eventRepository.save(event);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(savedEvent);
        log.info("#----- user updated event: {}", eventFullDto);
        return eventFullDto;
    }

    @Override
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id = " + eventId + " not found"));

        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.getCategory() != null) {
            Long categoryId = updateEventAdminRequest.getCategory();
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category with id = " + categoryId + " not found"));
            event.setCategory(category);
        }
        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }
        if (updateEventAdminRequest.getEventDate() != null) {
            if (event.getPublishedOn() != null &&
                    updateEventAdminRequest.getEventDate().isBefore(event.getPublishedOn().minusHours(1))) {
                throw new ConflictException("Event published on " + event.getPublishedOn() + " is after now");
            }
            event.setEventDate(updateEventAdminRequest.getEventDate());
        }
        if (updateEventAdminRequest.getLocation() != null) {
            event.setLon(updateEventAdminRequest.getLocation().getLon());
            event.setLat(updateEventAdminRequest.getLocation().getLat());
        }
        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }
        if (updateEventAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }
        if (updateEventAdminRequest.getStateAction() != null) {
            // TODO переписать вложенные условия
            if (updateEventAdminRequest.getStateAction() == EventStateActionAdmin.PUBLISH_EVENT &&
                    event.getState() == EventState.PENDING) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (updateEventAdminRequest.getStateAction() == EventStateActionAdmin.REJECT_EVENT
                    && event.getState() != EventState.PUBLISHED) {
                event.setState(EventState.CANCELED);
            } else {
                throw new ConflictException("Cannot publish the event because it's not in the right state: PUBLISHED");
            }
        }
        if (updateEventAdminRequest.getTitle() != null) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }
        Event savedEvent = eventRepository.save(event);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(savedEvent);

        // TODO добавление просмотров к dto

        log.info("#----- admin updated event: {}", eventFullDto);
        return eventFullDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id = " + userId + " not found"));

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

    private List<EventFullDto> makeEventDtoListWithViews(List<EventFullDto> events) {
        return events.stream()
                .peek(event -> event.setViews(1L))
                .collect(Collectors.toList());
    }
}
