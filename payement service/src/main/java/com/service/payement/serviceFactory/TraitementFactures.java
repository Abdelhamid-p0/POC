package com.service.payement.serviceFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.service.payement.dto.FactureKafka;
import com.service.payement.model.Odr;
import com.service.payement.repository.FactureRepository;
import com.service.payement.repository.OdrRepository;
import com.service.payement.service.CalculMontant;
import com.service.payement.service.FacturePretraitement;
import com.service.payement.service.PublierLog;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TraitementFactures {

    private final OdrRepository oDRRepository;
    private final FacturePretraitement facturePretraitement;
    private final CalculMontant calculMontant;
    private final PublierLog publierLog;


    TraitementFactures(FacturePretraitement facturePretraitement ,
                       CalculMontant calculMontant, OdrRepository oDRRepository, PublierLog publierLog) {
        this.facturePretraitement = facturePretraitement;
        this.calculMontant = calculMontant;
        this.oDRRepository = oDRRepository;
        this.publierLog = publierLog;
    }


    public void traitementFactures(String message , long i) throws JsonProcessingException {

        // pretraitement de message --> Liste de facture
        FactureKafka factureKafka = facturePretraitement.extraireFacturesFromMessageBatch(message);

            //Pub Log : facture n° i - Traitement commencer
           publierLog.publierTraitementFactureStart(i);
            //ecrire la facture en bd
            List<Odr> Odrs = facturePretraitement.enregistrerFactureDataBD(factureKafka);
            //Pub Log : facture n° i - Enregistrer en bd
            publierLog.publierSaveFacture(i);
            int j = 0;
            //Pour chaque acte==ODR
            for (Odr odr : Odrs) {
                j++;
                //Pub Log : facture n° i - Acte j
                publierLog.publierCalculMontantParActeStart(j);
                //Calcul du montant
                double montant = calculMontant.simulationCalculMontant(factureKafka,odr.getActe().getCodeActe());
                //Enregistrer l odr avec le montant correspondent
                odr.setMontant(montant);
                oDRRepository.save(odr);
                //Pub Log : facture n° i - Acte j - ODR enregistrer en bd
                publierLog.publierSaveODR(odr);

            }

        }

    }
