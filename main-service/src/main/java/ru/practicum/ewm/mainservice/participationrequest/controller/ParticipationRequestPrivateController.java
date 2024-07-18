package ru.practicum.ewm.mainservice.participationrequest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.mainservice.participationrequest.dto.ParticipationRequestDto;
import ru.practicum.ewm.mainservice.participationrequest.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Slf4j
public class ParticipationRequestPrivateController {
    private final ParticipationRequestService participationRequestService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
        log.info("*----- private get user requests userId = {}", userId);
        return participationRequestService.getUserRequests(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequest(@PathVariable Long userId,
                                                           @RequestParam Long eventId) {
        log.info("*----- private creating participation request: userId: {}, eventId: {}", userId, eventId);
        return participationRequestService.addParticipationRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        log.info("*----- private cancelling participation request: userId: {}, requestId: {}", userId, requestId);
        return participationRequestService.cancelRequest(userId, requestId);
    }
}
