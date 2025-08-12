package com.banque.bmce.service;

import com.banque.bmce.model.Facture;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class CalculMontant {

    public double simulationCalculMontant(Facture facture){
        Random random = new Random();

        return random.nextInt(1000)+50;

    }
}
