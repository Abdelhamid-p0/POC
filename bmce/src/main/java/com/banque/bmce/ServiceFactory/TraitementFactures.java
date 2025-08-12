package com.banque.bmce.ServiceFactory;

import com.banque.bmce.model.Acte;
import com.banque.bmce.model.Facture;
import com.banque.bmce.model.ODR;
import com.banque.bmce.repository.FactureRepository;
import com.banque.bmce.repository.ODRRepository;
import com.banque.bmce.service.CalculMontant;
import com.banque.bmce.service.FacturePretraitement;
import com.banque.bmce.service.PublierLog;
import org.springframework.stereotype.Service;

@Service
public class TraitementFactures {

    private final ODRRepository oDRRepository;
    private final FactureRepository factureRepository;
    private final PublierLog publierLog;
    private final FacturePretraitement facturePretraitement;
    private final CalculMontant calculMontant;


    TraitementFactures(FactureRepository factureRepository , PublierLog  publierLog,
                       FacturePretraitement facturePretraitement , CalculMontant calculMontant, ODRRepository oDRRepository) {
        this.factureRepository = factureRepository;
        this.publierLog = publierLog;
        this.facturePretraitement = facturePretraitement;
        this.calculMontant = calculMontant;
        this.oDRRepository = oDRRepository;
    }


    public void traitementFactures(String message) {

        // pretraitement de message --> Liste de facture
        Facture[] listeFacture = facturePretraitement.Pretraitement(message);
        //  Pub Log : N factures reçu
        publierLog.publierLotFactureRecu(listeFacture.length);
        int i = 0;
        // pour chaque facture :
        for (Facture facture : listeFacture) {
            i++;
          //Pub Log : facture n° i - Traitement commencer
          publierLog.publierTraitementFactureStart(i);
            //ecrire la facture en bd
            factureRepository.save(facture);
            //Pub Log : facture n° i - Enregistrer en bd
            publierLog.publierSaveFacture(i);
            //extraire la liste des actes à partir de la facture
            Acte[] actes = facture.listeActes ;
            int j = 0;
            //Pour chaque acte
            for (Acte acte : actes) {
                j++;
                //Pub Log : facture n° i - Acte j
                publierLog.publierCalculMontantParActe(j);
                //Calcul du montant
                double montant = calculMontant.simulationCalculMontant(facture);
                //générer un ODR pour chaque ligne d’acte
                ODR odr = new ODR(facture.id,acte,montant);
                oDRRepository.save(odr);
                //Pub Log : facture n° i - Acte j - ODR enregistrer en bd
                publierLog.publierSaveODR(j);


            }

        }
        System.out.println(message);

    }
}
