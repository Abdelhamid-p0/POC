package com.service.payement.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@Entity
public class Beneficiaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idBenif;

    private String nom;
    private String prenom;

    @Column(unique = true, nullable = false)
    private String rib;

    private BigDecimal credit;

    @OneToMany(mappedBy = "beneficiaire", cascade = CascadeType.ALL)
    private List<Facture> factures;

}
