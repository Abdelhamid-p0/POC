package com.service.payement.ListenerFactures;

import com.service.payement.dashboardApi.controller.DashboardController;
import com.service.payement.dashboardApi.service.DashboardService;
import com.service.payement.model.BatchStatistique;
import com.service.payement.model.SingleStatistique;
import com.service.payement.serviceFactory.TraitementFactures;
import com.service.payement.service.PublierLog;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;
import java.util.List;




@Component
public class FacturesConsommateur {

    TraitementFactures traitementFactures;
    PublierLog publierLog;
    public static ConsumerRecord<String, String> currentRecord ;
    private static final Logger log = LoggerFactory.getLogger(FacturesConsommateur.class);
    BatchStatistique batchStatistique;
    SingleStatistique  singleStatistique;
    private long startTime;
    int singleIndexRecord = 0;
    int batchIndexRecord = 0;
    DashboardService dashboardService;

    public void startChrono() {
        startTime = System.currentTimeMillis();
    }

    public long stopChrono() {
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("Temps écoulé : " + elapsed / 1000 + " secondes");

        return elapsed ;
    }



    FacturesConsommateur(TraitementFactures traitementFactures, PublierLog publierLog, BatchStatistique batchStatistique,
                         SingleStatistique singleStatistique,  DashboardService dashboardService) {
        this.traitementFactures = traitementFactures;
        this.publierLog = publierLog;
        this.batchStatistique = batchStatistique;
        this.singleStatistique = singleStatistique;
        this.dashboardService = dashboardService;
    }

    @KafkaListener(id = "singleListener", topics = "factures", containerFactory = "kafkaListenerSingleContainerFactory",
            autoStartup = "false")
    public void listenSingle(ConsumerRecord<String, String> record, Acknowledgment ack)  {

        if (singleIndexRecord == 0) {
            startChrono();
        }

        singleIndexRecord ++;


        log.info("Mode Single | message recu ");


        System.out.println("facture reçu : " + record);

        publierLog.publierSingleFactureRecu();

            currentRecord = record;
            try {
                traitementFactures.traitementFactures(record.value(), record.offset());
            } catch (Exception e) {

                throw new ListenerExecutionFailedException(
                        "Erreur lors du traitement du record: " + record.value(),e
                );
            }
        ack.acknowledge();


            if (singleIndexRecord==100){

                log.info("-----------------100 FACTURE-----------");
                singleStatistique.setTimer(stopChrono());
                dashboardService.ajouterSingleStat(singleStatistique);
                System.out.println(singleStatistique.getTimer());
                singleIndexRecord=0;

            }

        if (singleIndexRecord==60 && dashboardService.isThrowException()){
            throw new RuntimeException(
                    new SocketTimeoutException("⏱️ Timeout simulé : connexion réseau interrompue")
            );
        }

    }



    @KafkaListener( id = "batchListener", topics = "factures", containerFactory = "kafkaListenerBatchContainerFactory",
            autoStartup = "false")
    public void listenBatch(List<ConsumerRecord<String, String>> records, Acknowledgment ack)  {


        log.info("Mode Batch | message recu ");

        startChrono();

        System.out.println("Batch reçu : " + records.size() + " messages");

        publierLog.publierLotFactureRecu(records.size());

        batchStatistique.setBatchRecu(records.size());

        for (ConsumerRecord<String, String> record : records) {
            currentRecord = record;
            batchIndexRecord++;
            try {
                traitementFactures.traitementFactures(record.value(), record.offset());
                System.out.println("Index: " + batchIndexRecord);
                System.out.println("IsThrown: " + dashboardService.isThrowException());

                if (batchIndexRecord==60 && dashboardService.isThrowException()){

                    throw new RuntimeException(
                            new SocketTimeoutException("⏱️ Timeout simulé : connexion réseau interrompue")
                    );

                }
                if (batchIndexRecord==100){
                    batchIndexRecord = 0;
                }
            } catch (Exception e) {

                throw new ListenerExecutionFailedException(
                        "Erreur lors du traitement du record: " + record.value(),e
                );
            }
        }

        ack.acknowledge();

        batchStatistique.setTimer(stopChrono());
        dashboardService.ajouterBatchStat(batchStatistique);
    }

}
    

    
