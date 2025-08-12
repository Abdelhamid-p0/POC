package com.banque.bmce.service;

import com.banque.bmce.model.Facture;
import org.springframework.stereotype.Service;

@Service
public class FacturePretraitement {

    public Facture[] Pretraitement(String message) {

        return new Facture[2];
    }
}
