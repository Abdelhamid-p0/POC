package com.service.payement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@Data
@NoArgsConstructor   // constructeur vide
@AllArgsConstructor  // constructeur avec tous les champs
public class SingleStatistique {

    private long timer;
}
