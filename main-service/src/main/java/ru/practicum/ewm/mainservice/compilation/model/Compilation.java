package ru.practicum.ewm.mainservice.compilation.model;

import lombok.Data;
import org.hibernate.validator.constraints.UniqueElements;
import ru.practicum.ewm.mainservice.event.model.Event;

import javax.persistence.*;
import java.util.Set;

@Entity
@Data
@Table(name = "compilations")
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Список идентификаторов событий входящих в подборку
    @ManyToMany
    @JoinTable(name = "events_compilations",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id"))
    @UniqueElements
    private Set<Event> events;

    // Закреплена ли подборка на главной странице сайта
    @Column(nullable = false)
    private Boolean pinned;

    // Заголовок подборки
    @Column(nullable = false)
    private String title;
}
