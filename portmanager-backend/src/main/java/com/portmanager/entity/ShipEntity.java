package com.portmanager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity @Table(name = "ships")
@Getter @Setter
public class ShipEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private double length;
    private double draft;
    private String cargoType;

    /** ETA stored in UTC. */
    private OffsetDateTime eta;

    @Column(nullable = true)
    private double estDurationHours;
    private String priority;

    @Column(nullable = true)
    private double  deadweight;

    @Column(length = 64)
    private String  flagCountry;

    @Column(length = 16)
    private String  imoNumber;

    @Column(length = 32)
    private String  shipType;

    @Column(nullable = true)
    private boolean requiresCustomsClearance;

    @Column(nullable = true)
    private String  hazardClass;

    @Column(nullable = true)
    private boolean temperatureControlled;

    @Column(length = 32)
    private String  fuelType;

    private String  emissionRating;

    @Column(length = 32)
    private String  arrivalPort;

    @Column(length = 32)
    private String  nextPort;

    @Column(nullable = true)
    private boolean requiresPilot;

    private OffsetDateTime arrivalWindowStart;
    private OffsetDateTime arrivalWindowEnd;

    @Column(nullable = true)
    private double expectedDelayHours;
}
