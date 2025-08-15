package com.PestationSante.prestationSante.model;

import java.time.LocalDate;
import java.util.ArrayList;

public class Facture {
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


    public Facture() {

    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public void setBenificiare(String benificiare) {
        this.benificiare = benificiare;
    }

    public void setActes(ArrayList<String> actes) {
        this.actes = actes;
    }

    public void setProf_sante(String prof_sante) {
        this.prof_sante = prof_sante;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOrganisation() {
        return this.organisation;
    }

    public String getBenificiare() {
        return this.benificiare;
    }

    public String getProf_sante() {
        return this.prof_sante;
    }

    public ArrayList<String> getActes() {
        return this.actes;
    }

    public String getDate() {
        return this.date;
    }
}
