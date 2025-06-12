package com.portmanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "terminals")
@Getter @Setter
public class TerminalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double maxLength;
    private double maxDraft;

    /** Перечень грузов, которые может обрабатывать терминал. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "terminal_cargo_types",
            joinColumns = @JoinColumn(name = "terminal_id"))
    @Column(name = "cargo_type")
    private List<String> cargoTypes = new ArrayList<>();
}
