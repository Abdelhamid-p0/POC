package com.service.payement.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Acte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idActe;

    private String codeActe;


    @OneToMany(mappedBy = "acte", cascade = CascadeType.ALL)
    private List<Odr> odrs;


}
