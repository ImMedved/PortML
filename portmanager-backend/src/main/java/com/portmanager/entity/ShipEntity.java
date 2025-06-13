package com.portmanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * ShipEntity — DB row of vessel waiting for berth.
 */
@Entity
@Table(name = "ships")
@Getter @Setter
public class ShipEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double length;
    private double draft;
    private String cargoType;

    /** ETA in UTC. */
    private OffsetDateTime eta;
    private double estDurationHours;
    private String priority;
}
