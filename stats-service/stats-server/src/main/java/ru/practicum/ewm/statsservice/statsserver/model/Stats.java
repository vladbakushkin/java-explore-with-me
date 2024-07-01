package ru.practicum.ewm.statsservice.statsserver.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "stats")
public class Stats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String app;

    @Column(nullable = false)
    private String uri;

    @Column(nullable = false)
    private String ip;

    @Column(name = "request_time", nullable = false)
    private LocalDateTime requestTime;
}
