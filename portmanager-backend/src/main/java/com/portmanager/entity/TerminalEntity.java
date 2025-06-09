package com.portmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/**
 * TerminalEntity
 *
 * Table `terminal` – static berth capabilities.
 */
@Entity
@Table(name = "terminal")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TerminalEntity {

    @Id
    private Integer id;

    private String name;

    private double maxLength;
    private double maxDraft;

    /** Allowed cargo types – stored in side-table `terminal_allowed_cargo`. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "terminal_allowed_cargo",
            joinColumns = @JoinColumn(name = "terminal_id"))
    @Column(name = "cargo_type")
    private List<String> allowedCargoTypes;
}
