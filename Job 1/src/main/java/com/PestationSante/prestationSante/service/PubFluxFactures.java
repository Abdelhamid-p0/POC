package com.PestationSante.prestationSante.service;

import com.PestationSante.prestationSante.model.Facture;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class PubFluxFactures {

    public static final ObjectMapper mapper = new ObjectMapper();
    private final KafkaTemplate<String, String> kafkaTemplate;

    public PubFluxFactures(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // --- Cas normal : toutes les factures valides ---
    public ObjectNode simulerNAppelsSequenciels(int n, Facture facture) {
        ObjectNode response = mapper.createObjectNode();
        response.put("nbrsFactures", n);
        response.put("nbrsFacturesErr", 0);

        try {
            for (int i = 1; i <= n; i++) {
                PublierFluxFactures(facture, i);
            }
            response.put("status", "success");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "failed");
        }
        return response;
    }

    // --- Cas avec erreurs sur messages 20 et 90 ---
    public ObjectNode simulerNAppelsSequencielsAvecErreurs(int n, Facture facture) {
        ObjectNode response = mapper.createObjectNode();
        response.put("nbrsFactures", n);
        response.put("nbrsFacturesErr", 2);

        try {
            for (int i = 1; i <= n; i++) {
                PublierFluxFacturesAvecErreur(facture, i, response);
            }
            response.put("status", "success");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "failed");
        }
        return response;
    }

    // --- Publication normale ---
    private void PublierFluxFactures(Facture facture, int index) throws JsonProcessingException {
        System.out.println("Facture " + index + " : " + facture.toString());

        CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send("factures", mapper.writeValueAsString(facture));

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("✅ Message envoyé à partition " +
                        result.getRecordMetadata().partition() +
                        " offset " + result.getRecordMetadata().offset());
            } else {
                System.err.println("❌ Erreur envoi Kafka: " + ex.getMessage());
            }
        });
    }

    // --- Publication avec injection d’erreurs ---
    private void PublierFluxFacturesAvecErreur(Facture facture, int index, ObjectNode response) throws JsonProcessingException {

        String payload;
        if (index == 20 || index == 90) {
            // JSON volontairement corrompu
            payload = "{invalidJson: true, factureBenificiaire=" + facture.getBenificiare() + "}";
            response.put("status", "error_injected");
            response.put("factureErrIndex", index);
            response.put("nbrsFacturesErr", response.get("nbrsFacturesErr").asInt() + 1);
        } else {
            payload = mapper.writeValueAsString(facture);
        }

        System.out.println("Facture " + index + " : " + payload);

        CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send("factures", payload);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("✅ Message envoyé à partition " +
                        result.getRecordMetadata().partition() +
                        " offset " + result.getRecordMetadata().offset());
            } else {
                System.err.println("❌ Erreur envoi Kafka: " + ex.getMessage());
            }
        });
    }
}
