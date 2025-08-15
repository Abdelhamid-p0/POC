package com.service.payement.ServiceFactory;

import com.service.payement.model.FactureKafka;
import com.service.payement.model.Odr;
import com.service.payement.repository.FactureRepository;
import com.service.payement.repository.OdrRepository;
import com.service.payement.service.CalculMontant;
import com.service.payement.service.FacturePretraitement;
import com.service.payement.service.PublierLog;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class TraitementFactures {

    private final OdrRepository oDRRepository;
    private final FactureRepository factureRepository;
    private final PublierLog publierLog;
    private final FacturePretraitement facturePretraitement;
    private final CalculMontant calculMontant;


    TraitementFactures(FactureRepository factureRepository , PublierLog  publierLog,
                       FacturePretraitement facturePretraitement , CalculMontant calculMontant, OdrRepository oDRRepository) {
        this.factureRepository = factureRepository;
        this.publierLog = publierLog;
        this.facturePretraitement = facturePretraitement;
        this.calculMontant = calculMontant;
        this.oDRRepository = oDRRepository;
    }


    public void traitementFactures(String message) {

        // pretraitement de message --> Liste de facture
        FactureKafka[] listeFactures = facturePretraitement.extraireFacturesFromMessage(message);
        //  Pub Log : N factures reçu
        publierLog.publierLotFactureRecu(listeFactures.length);
        int i = 0;
        // pour chaque facture :
        for (FactureKafka factureKafka : listeFactures) {
            i++;
            //Pub Log : facture n° i - Traitement commencer
            publierLog.publierTraitementFactureStart(i);
            //ecrire la facture en bd
            facturePretraitement.enregistrerFactureDataBD(factureKafka);
            //Pub Log : facture n° i - Enregistrer en bd
            publierLog.publierSaveFacture(i);
            //extraire la liste des actes à partir de la facture
            ArrayList<String> actes = factureKafka.getActes() ;
            int j = 0;
            //Pour chaque acte
            for (String acte : actes) {
                j++;
                //Pub Log : facture n° i - Acte j
                publierLog.publierCalculMontantParActeStart(j);
                //Calcul du montant
                double montant = calculMontant.simulationCalculMontant(factureKafka);
                //générer un ODR pour chaque ligne d’acte
                Odr odr = new Odr(factureKafka.id,acte,montant);
                oDRRepository.save(odr);
                //Pub Log : facture n° i - Acte j - ODR enregistrer en bd
                publierLog.publierSaveODR(j);


            }

        }
        System.out.println(message);

    }
}
