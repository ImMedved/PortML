package com.portmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * ShipEntity
 *
 * Table `ship` – a single vessel call (ETA + physical params).
 */
@Entity
@Table(name = "ship")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShipEntity {

    @Id
    private String id;

    private OffsetDateTime arrivalTime;
    private double length;
    private double draft;
    private String cargoType;
    private double estDurationHours;
}
