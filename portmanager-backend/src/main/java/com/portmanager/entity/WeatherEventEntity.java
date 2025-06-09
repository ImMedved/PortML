package com.portmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * WeatherEventEntity
 *
 * Table `weather_event` – port-wide event that blocks operations.
 */
@Entity
@Table(name = "weather_event")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WeatherEventEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private OffsetDateTime startTime;
    private OffsetDateTime endTime;

    @Column(length = 256)
    private String description;
}
