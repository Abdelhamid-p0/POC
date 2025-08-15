package com.PestationSante.prestationSante.service;

import com.PestationSante.prestationSante.model.Facture;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


@Service
public class PubFluxFactures {

    public static final ObjectMapper mapper = new ObjectMapper();
    private final KafkaTemplate<String, String> kafkaTemplate;
    ObjectNode response = mapper.createObjectNode();

    public PubFluxFactures(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    public ObjectNode simulerNAppelsParalleles(int n, Facture facture) {
        ExecutorService executor = Executors.newFixedThreadPool(n); // nthreads

        List<Callable<ObjectNode>> tasks = new ArrayList<>();

            response.put("nbrsFactures", n);
            response.put("nbrsFacturesErr", 0);

        for (int i = 1; i <= n; i++) {
            tasks.add(() -> PublierFluxFactures(facture));
        }

        try {
            List<Future<ObjectNode>> futures = executor.invokeAll(tasks);
            return futures.get(n - 1).get(); // récupérer le nᵉ (index n-1)

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ObjectMapper().createObjectNode().put("error", "Erreur lors des appels parallèles.");
        } finally {
            executor.shutdown();
        }
    }


    public ObjectNode PublierFluxFactures(Facture facture) throws JsonProcessingException {

            System.out.println("Facture:" + facture.toString());
            kafkaTemplate.send("factures",mapper.writeValueAsString(facture));

            response.put("status", "success");

            return response ;

    }

}