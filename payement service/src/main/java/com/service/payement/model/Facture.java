package com.service.payement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFacture;

    private String organisation;
    private String professionnelDeSante;
    private LocalDate dateSoin;

    @ManyToOne
    @JoinColumn(name = "id_benif", nullable = false)
    private Beneficiaire beneficiaire;

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL)
    private List<Odr> odrs;

    @OneToOne(mappedBy = "facture", cascade = CascadeType.ALL)
    private Odp odp;


}


