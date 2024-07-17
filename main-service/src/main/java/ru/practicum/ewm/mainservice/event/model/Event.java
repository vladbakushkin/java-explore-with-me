package ru.practicum.ewm.mainservice.event.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.ewm.mainservice.category.model.Category;
import ru.practicum.ewm.mainservice.event.model.state.EventState;
import ru.practicum.ewm.mainservice.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Краткое описание события
    @Column(nullable = false)
    private String annotation;

    // категория к которой относится событие
    @JoinColumn(name = "category_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    // Полное описание события
    @Column(nullable = false, length = 7000)
    private String description;

    // Дата и время на которые намечено событие в формате "yyyy-MM-dd HH:mm:ss"
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    // Широта места проведения события
    @Column(nullable = false)
    private Float lat;

    // Долгота места проведения события
    @Column(nullable = false)
    private Float lon;

    // Нужно ли оплачивать участие в событии
    @Column(nullable = false)
    private Boolean paid;

    // Ограничение на количество участников. Значение 0 - отсутствие ограничения
    @Column(name = "participant_limit", nullable = false)
    private Integer participantLimit;

    // Нужна ли пре-модерация заявок на участие.
    // Если true, то все заявки будут ожидать подтверждения инициатором события.
    // Если false - то будут подтверждаться автоматически.
    @Column(name = "request_moderation", nullable = false)
    private Boolean requestModeration;

    // Заголовок события
    @Column(nullable = false)
    private String title;

    // Количество одобренных заявок на участие в данном событии
    @Column(name = "confirmed_requests", nullable = false)
    private Long confirmedRequests;

    // Список состояний жизненного цикла события
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventState state;

    // Дата и время создания события (в формате "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_on", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdOn;

    // Дата и время публикации события (в формате "yyyy-MM-dd HH:mm:ss")
    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    // Инициатор события
    @JoinColumn(name = "initiator_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User initiator;
}
