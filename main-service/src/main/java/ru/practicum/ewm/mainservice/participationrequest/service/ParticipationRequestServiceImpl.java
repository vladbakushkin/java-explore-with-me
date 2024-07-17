package ru.practicum.ewm.mainservice.participationrequest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.mainservice.event.model.Event;
import ru.practicum.ewm.mainservice.event.model.state.EventState;
import ru.practicum.ewm.mainservice.event.repository.EventRepository;
import ru.practicum.ewm.mainservice.exception.ConflictException;
import ru.practicum.ewm.mainservice.exception.NotFoundException;
import ru.practicum.ewm.mainservice.participationrequest.dto.ParticipationRequestDto;
import ru.practicum.ewm.mainservice.participationrequest.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.mainservice.participationrequest.model.ParticipationRequest;
import ru.practicum.ewm.mainservice.participationrequest.model.RequestStatus;
import ru.practicum.ewm.mainservice.participationrequest.repository.ParticipationRequestRepository;
import ru.practicum.ewm.mainservice.user.model.User;
import ru.practicum.ewm.mainservice.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private final ParticipationRequestRepository participationRequestRepository;
    private final ParticipationRequestMapper participationRequestMapper;

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id = " + userId + " not found"));

        List<ParticipationRequest> participationRequests = participationRequestRepository.findAllByRequester(user);
        return participationRequestMapper.toParticipationRequestDtoList(participationRequests);
    }

    @Override
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id = " + userId + " not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id = " + eventId + " not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("You cannot participate in an unpublished event");
        }

        if (userId.equals(event.getInitiator().getId())) {
            throw new ConflictException("Initiator cannot add a request to participate in their event");
        }

        ParticipationRequest participationRequest = new ParticipationRequest();
        participationRequest.setRequester(user);
        participationRequest.setEvent(event);
        participationRequest.setStatus(event.getRequestModeration() ? RequestStatus.PENDING : RequestStatus.CONFIRMED);

        if (event.getParticipantLimit() == 0) {
            // 0 - no limits
            participationRequest.setStatus(RequestStatus.CONFIRMED);
        }

        int participantCount = participationRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 &&
                event.getParticipantLimit() <= participantCount) {
            throw new ConflictException("The participant limit has been reached");
        }

        ParticipationRequest savedParticipationRequest = participationRequestRepository.save(participationRequest);
        ParticipationRequestDto result =
                participationRequestMapper.toParticipationRequestDto(savedParticipationRequest);
        log.info("#----- added participation request {}, ", result);
        return result;
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id = " + userId + " not found"));

        ParticipationRequest request = participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id = " + requestId + " not found"));

        request.setStatus(RequestStatus.CANCELED);

        ParticipationRequest savedRequest = participationRequestRepository.save(request);
        ParticipationRequestDto requestDto = participationRequestMapper.toParticipationRequestDto(savedRequest);
        log.info("#----- private canceled request {}, ", requestDto);
        return requestDto;
    }
}
