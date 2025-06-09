package com.portmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * TerminalClosureEntity
 *
 * Table `terminal_closure` – maintenance or outage window for a berth.
 */
@Entity
@Table(name = "terminal_closure")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TerminalClosureEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "terminal_id", nullable = false)
    private TerminalEntity terminal;

    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String reason;
}
