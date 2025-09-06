package com.service.payement.dashboardApi.service;

import com.service.payement.model.BatchStatistique;
import com.service.payement.model.Log;
import com.service.payement.model.SingleStatistique;
import lombok.Getter;
import lombok.Setter;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Getter
@Setter
public class DashboardService {

    private final KafkaListenerEndpointRegistry registry;

    private final List<String> dlqFactures = new ArrayList<>();

    private  String batchListnnerStatus = "Inactive";

    private  String singleListnnerStatus = "Inactive";

    private String thrownExceptionStatus = "Inactive";

    private boolean throwException = false;

    private List<BatchStatistique> batchStatistiqueList = new ArrayList<>();

    private List<SingleStatistique> singleStatistiqueList = new ArrayList<>();

    private List<Log> logList = new ArrayList<>();

    public DashboardService(KafkaListenerEndpointRegistry registry) {
        this.registry = registry;
    }

    public void switchMode(String mode) {
        var batchContainer = registry.getListenerContainer("batchListener");
        var singleContainer = registry.getListenerContainer("singleListener");

        System.out.println("batchContainer = " + batchContainer);
        System.out.println("singleContainer = " + singleContainer);

        if ("batch".equalsIgnoreCase(mode)) {
            System.out.println("Mode: " + mode);

            registry.getListenerContainer("singleListener").stop();
            registry.getListenerContainer("batchListener").start();

            batchContainer = registry.getListenerContainer("batchListener");
            singleContainer = registry.getListenerContainer("singleListener");

            System.out.println("batchContainer = " + batchContainer);
            System.out.println("singleContainer = " + singleContainer);

            batchListnnerStatus = "Active";
            singleListnnerStatus = "Inactive";

        }
        else if ("single".equalsIgnoreCase(mode)) {
            System.out.println("Mode: " + mode);
            registry.getListenerContainer("batchListener").stop();

            registry.getListenerContainer("singleListener").start();


            batchContainer = registry.getListenerContainer("batchListener");
            singleContainer = registry.getListenerContainer("singleListener");

            System.out.println("batchContainer = " + batchContainer);
            System.out.println("singleContainer = " + singleContainer);

            singleListnnerStatus = "Active";
            batchListnnerStatus = "Inactive";

        }
        else {
            registry.getListenerContainer("batchListener").stop();
            registry.getListenerContainer("singleListener").stop();

            singleListnnerStatus = "Inactive";
            batchListnnerStatus = "Inactive";

        }
    }

    public void ajouterFactureDLQ(String dlqFacture) {
        dlqFactures.add(dlqFacture);
    }

    public List<String> getFactureDLQ() {
        return dlqFactures;
    }

    public void leverException() {
        throwException = !throwException;
    }

    public String getThrownExceptionStatus(){
        return thrownExceptionStatus = throwException? "Active" : "Inactive";
    }

    public void ajouterBatchStat(BatchStatistique batchStatistique) {
        batchStatistiqueList.add(new BatchStatistique(batchStatistique.getBatchRecu(), batchStatistique.getTimer()));
    }

    public void ajouterSingleStat(SingleStatistique singleStatistique) {
        singleStatistiqueList.add(new SingleStatistique(singleStatistique.getTimer()));
    }

    public void ajouterLog(Log log) {
        logList.add(new Log(log.getNiveau(), log.getMessage()));
    }

}
