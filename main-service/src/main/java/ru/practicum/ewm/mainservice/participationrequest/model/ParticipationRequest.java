package ru.practicum.ewm.mainservice.participationrequest.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.ewm.mainservice.event.model.Event;
import ru.practicum.ewm.mainservice.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "participation_requests")
public class ParticipationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "event_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Event event;

    @JoinColumn(name = "requester_id")
    @OneToOne
    private User requester;

    @Column
    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @Column(name = "created_on")
    @CreationTimestamp
    private LocalDateTime createdOn;
}
