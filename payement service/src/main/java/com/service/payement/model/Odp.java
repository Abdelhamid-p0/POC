package com.service.payement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class Odp {

    // Getters & Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOdp;

    private BigDecimal montant;
    private String status;
    private String ribBenif;
    private LocalDateTime dateEnregistrement;

    @OneToOne
    @JoinColumn(name = "id_facture", nullable = false)
    private Facture facture;

}
