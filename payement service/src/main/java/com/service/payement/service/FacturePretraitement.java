package com.service.payement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.payement.dto.FactureKafka;
import com.service.payement.model.*;
import com.service.payement.repository.ActeRepository;
import com.service.payement.repository.FactureRepository;
import com.service.payement.repository.OdrRepository;
import com.service.payement.repository.BeneficiaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class FacturePretraitement {

    private final FactureRepository factureRepository;
    private final OdrRepository odrRepository;
    private final ActeRepository acteRepository;
    private final BeneficiaireRepository beneficiaireRepository;
    PublierLog publierLog;
    @Autowired
    private ObjectMapper mapper;

    public FacturePretraitement(FactureRepository factureRepository, OdrRepository odrRepository,
                                ActeRepository acteRepository, BeneficiaireRepository beneficiaireRepository,
                                PublierLog publierLog) {
        this.factureRepository = factureRepository;
        this.odrRepository = odrRepository;
        this.acteRepository = acteRepository;
        this.beneficiaireRepository = beneficiaireRepository;
        this.publierLog = publierLog;
    }

    public FactureKafka extraireFacturesFromMessageBatch(String message) throws JsonProcessingException {

            FactureKafka facture = mapper.readValue(message, FactureKafka.class);

        return facture;
    }



    @Transactional
    public List<Odr> enregistrerFactureDataBD(FactureKafka fk) {

        // 1️⃣ Récupérer le bénéficiaire
        System.out.println(fk.toString());
        Beneficiaire benef = beneficiaireRepository.findByNom(fk.getBenificiare());
        publierLog.publierRecupererBenificiaireDuFacture(benef);

        // 2️⃣ Créer la facture
        Facture facture = new Facture();
        facture.setOrganisation(fk.getOrganisation());
        facture.setProfessionnelDeSante(fk.getProf_sante());
        facture.setBeneficiaire(benef);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
        LocalDate date = LocalDate.parse(fk.getDate(), formatter);
        facture.setDateSoin(date);
        factureRepository.save(facture);
        publierLog.publierSaveFactureEntity(facture);
        // 3️⃣ Créer les ODRs avec montant temporaire -1
        List<Odr> odrList = new ArrayList<>();
        for(String codeActe : fk.getActes()) {
            Acte acte = acteRepository.findByCodeActe(codeActe);

            Odr odr = new Odr();
            odr.setFacture(facture);
            odr.setActe(acte);
            odr.setMontant(-1); // temporaire
            odrList.add(odr);
            publierLog.publierSaveODR(odr);
        }
        odrRepository.saveAll(odrList);

        return odrList;

    }



}
