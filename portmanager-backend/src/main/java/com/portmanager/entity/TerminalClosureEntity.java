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

    /** ID of the terminal being closed */
    private Long terminalId;

    private OffsetDateTime startTime;
    private OffsetDateTime endTime;

    /** Additional description (optional). */
    private String reason;
}
