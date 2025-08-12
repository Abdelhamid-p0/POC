package com.banque.bmce.model;

public class ODR {

    int id;
    double montant;
    Acte acte;
    int factureId;

    public ODR(int factureId, Acte acte, double montant) {

        this.factureId = factureId;
        this.acte = acte;
        this.montant = montant;
    }
}
