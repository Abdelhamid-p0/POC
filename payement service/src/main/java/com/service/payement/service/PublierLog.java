package com.service.payement.service;

import com.service.payement.dashboardApi.controller.DashboardController;
import com.service.payement.dashboardApi.service.DashboardService;
import com.service.payement.model.Beneficiaire;
import com.service.payement.model.Facture;
import com.service.payement.model.Log;
import com.service.payement.model.Odr;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PublierLog {

    private final DashboardService dashboardService;

    public PublierLog(DashboardController dashboardController, DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    public void publierSaveFacture(long i) {
        dashboardService.ajouterLog(new Log("INFO", "Données de facture enregistrée (id=" + i + ")"));
    }

    public void publierLotFactureRecu(int i) {
        dashboardService.ajouterLog(new Log("INFO", i + " factures reçues"));
    }

    public void publierSingleFactureRecu() {
        dashboardService.ajouterLog(new Log("INFO", "Facture unique reçue"));
    }

    public void publierRecupererBenificiaireDuFacture(Beneficiaire beneficiaire) {
        dashboardService.ajouterLog(new Log("INFO", "Récupération bénéficiaire du facture: " + beneficiaire));
    }

    public void publierTraitementFactureStart(long i) {
        dashboardService.ajouterLog(new Log("INFO", "Facture n°" + i + " - Traitement commencé"));
    }

    public void publierCalculMontantParActeStart(int j) {
        dashboardService.ajouterLog(new Log("INFO", "Calcul du montant (acte=" + j + ")"));
    }

    public void publierSaveODR(Odr odr) {
        dashboardService.ajouterLog(new Log("INFO", "Entité ODR enregistrée: " + odr));
    }

    public void publierSaveFactureEntity(Facture facture) {
        dashboardService.ajouterLog(new Log("INFO", "Entité Facture enregistrée: " + facture));
    }

    public void publierSendToDLQ(ConsumerRecord<?, ?> record, Exception exception) {
        dashboardService.ajouterLog(new Log("ERROR",
                "📦 DLQ: topic=" + record.topic() +
                        ", partition=" + record.partition() +
                        ", offset=" + record.offset() +
                        ", key=" + record.key() +
                        ", value=" + record.value() +
                        ", exception=" + exception.getMessage()
        ));
    }

    public void publierExceptionNonRecoverableLeve(String exception) {
        dashboardService.ajouterLog(new Log("ERROR", "Exception NON RÉCUPÉRABLE levée: " + exception));
    }

    public void publierExceptionRecoverableLeve(String exception) {
        dashboardService.ajouterLog(new Log("ERROR", "Exception RÉCUPÉRABLE levée: " + exception));
    }
}
