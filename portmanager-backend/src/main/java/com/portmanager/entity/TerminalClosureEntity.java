package com.portmanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "terminal_closure")
@Getter @Setter
public class TerminalClosureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID терминала, который закрывается  */
    private Long terminalId;

    private OffsetDateTime startTime;
    private OffsetDateTime endTime;

    /** Доп. описание (optional). */
    private String reason;
}
