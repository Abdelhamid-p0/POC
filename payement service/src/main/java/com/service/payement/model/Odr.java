package com.service.payement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class Odr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOdr;

    private double montant;

    @ManyToOne
    @JoinColumn(name = "id_facture", nullable = false)
    private Facture facture;

    @ManyToOne
    @JoinColumn(name = "code_acte", nullable = false)
    private Acte acte;
}


