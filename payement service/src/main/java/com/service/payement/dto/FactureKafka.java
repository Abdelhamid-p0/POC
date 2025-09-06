package com.service.payement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class FactureKafka {
    String organisation;
    String benificiare;
    String prof_sante;
    ArrayList<String> actes;
    String date;


    @Override
    public String toString() {
        return "Facture{" +
                "organisation='" + organisation + '\'' +
                ", benificiare='" + benificiare + '\'' +
                ", prof_sante='" + prof_sante + '\'' +
                ", actes=" + actes +
                ", date=" + date +
                '}';
    }


    public FactureKafka() {

    }

}