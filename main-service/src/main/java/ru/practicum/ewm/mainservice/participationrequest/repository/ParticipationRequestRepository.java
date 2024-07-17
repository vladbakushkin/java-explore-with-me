package ru.practicum.ewm.mainservice.participationrequest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.mainservice.event.model.Event;
import ru.practicum.ewm.mainservice.participationrequest.model.ParticipationRequest;
import ru.practicum.ewm.mainservice.participationrequest.model.RequestStatus;
import ru.practicum.ewm.mainservice.user.model.User;

import java.util.List;

@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findAllByRequester(User requester);

    List<ParticipationRequest> findAllByEvent(Event event);

    int countByEventIdAndStatus(Long eventId, RequestStatus status);
}
