package com.portmanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ships")
@Getter @Setter
public class ShipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double length;
    private double draft;
    private String cargoType;
    private OffsetDateTime eta;
}
