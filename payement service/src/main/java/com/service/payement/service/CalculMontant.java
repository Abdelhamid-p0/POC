package com.service.payement.service;

import com.service.payement.dto.FactureKafka;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class CalculMontant {

    public double simulationCalculMontant(FactureKafka facture , String acte){
        Random random = new Random();

        return random.nextInt(1000)+50;

    }
}
