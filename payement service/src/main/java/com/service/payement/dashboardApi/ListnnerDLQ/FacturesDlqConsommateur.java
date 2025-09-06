package com.service.payement.dashboardApi.ListnnerDLQ;

import com.service.payement.dashboardApi.service.DashboardService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FacturesDlqConsommateur {

    DashboardService dashboardService ;

    FacturesDlqConsommateur(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @KafkaListener(id = "DlqListener", topics = "facture-DLQ", containerFactory = "kafkaListenerDLQContainerFactory",
            autoStartup = "true")
    public void listenSingle(ConsumerRecord<String, String> record)  {

        dashboardService.ajouterFactureDLQ(record.value());

    }
}
