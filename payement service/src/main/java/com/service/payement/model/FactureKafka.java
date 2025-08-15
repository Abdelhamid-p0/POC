package com.service.payement.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class FactureKafka {
    String organisation;
    String benificiareNom;
    String benificiarePrenom;
    String prof_sante;
    ArrayList<String> actes;
    String date;


    @Override
    public String toString() {
        return "Facture{" +
                "organisation='" + organisation + '\'' +
                ", benificiareNom='" + benificiareNom + '\'' +
                ", benificiarePrenom='" + benificiarePrenom + '\'' +
                ", prof_sante='" + prof_sante + '\'' +
                ", actes=" + actes +
                ", date=" + date +
                '}';
    }


    public FactureKafka() {

    }

}