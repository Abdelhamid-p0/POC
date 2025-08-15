package com.service.payement.service;

import com.service.payement.model.*;
import com.service.payement.repository.ActeRepository;
import com.service.payement.repository.FactureRepository;
import com.service.payement.repository.OdrRepository;
import com.service.payement.repository.BeneficiaireRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class FacturePretraitement {

    private final FactureRepository factureRepository;
    private final OdrRepository odrRepository;
    private final ActeRepository acteRepository;
    private final BeneficiaireRepository beneficiaireRepository;

    public FacturePretraitement(FactureRepository factureRepository, OdrRepository odrRepository, ActeRepository acteRepository, BeneficiaireRepository beneficiaireRepository) {
        this.factureRepository = factureRepository;
        this.odrRepository = odrRepository;
        this.acteRepository = acteRepository;
        this.beneficiaireRepository = beneficiaireRepository;
    }

    public FactureKafka[] extraireFacturesFromMessage(String message) {

        return new FactureKafka[2];
    }

    @Transactional
    public void enregistrerFactureDataBD(FactureKafka fk) {

        // 1️⃣ Récupérer le bénéficiaire
        Beneficiaire benef = beneficiaireRepository.findByNom(fk.getBenificiareNom());

        // 2️⃣ Créer la facture
        Facture facture = new Facture();
        facture.setOrganisation(fk.getOrganisation());
        facture.setProfessionnelDeSante(fk.getProf_sante());
        facture.setBeneficiaire(benef);
        facture.setDateSoin(LocalDate.parse(fk.getDate()));
        factureRepository.save(facture);

        // 3️⃣ Créer les ODRs avec montant temporaire -1
        List<Odr> odrList = new ArrayList<>();
        for(String codeActe : fk.getActes()) {
            Acte acte = acteRepository.findByCodeActe(codeActe);

            Odr odr = new Odr();
            odr.setFacture(facture);
            odr.setActe(acte);
            odr.setMontant(-1); // temporaire
            odrList.add(odr);
        }

        odrRepository.saveAll(odrList);

    }


}
