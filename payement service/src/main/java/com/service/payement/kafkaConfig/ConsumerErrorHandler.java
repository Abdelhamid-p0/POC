package com.service.payement.kafkaConfig;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.service.payement.service.PublierLog;
import lombok.Setter;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.KafkaUtils;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.BackOffExecution;
import org.springframework.util.backoff.ExponentialBackOff;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.service.payement.ListenerFactures.FacturesConsommateur.currentRecord;
import static com.service.payement.service.PublierLog.*;

public class ConsumerErrorHandler implements CommonErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ConsumerErrorHandler.class);

    private final int maxRetries;
    private final long initialInterval;
    private final double multiplier;
    private final PublierLog publierLog;
    @Setter
    private KafkaTemplate<String, String> kafkaTemplate;


        public ConsumerErrorHandler(int maxRetries, long initialInterval, double multiplier, PublierLog publierLog) {
        this.maxRetries = maxRetries;
        this.initialInterval = initialInterval;
        this.multiplier = multiplier;
            this.publierLog = publierLog;
        }

    // --- Gestion d'un seul record fautif ---
    private void handleSingleRecord(ConsumerRecord<?, ?> record, Exception exception, Consumer<?, ?> consumer) {
        try {
            Throwable rootCause = getRootCause(exception);

            if (rootCause instanceof DeserializationException) {
                log.error("Message non désérialisable → DLQ direct: offset={} exception={}", record.offset(), rootCause.getMessage());
                publierLog.publierExceptionNonRecoverableLeve("DeserializationException");
                sendToDLQ(record, exception);
                return;
            }

            if (rootCause instanceof JsonParseException) {
                log.error("Message non traité à cause de JsonParseException  → DLQ direct: offset={} exception={}", record.offset(), rootCause.getMessage());
                publierLog.publierExceptionNonRecoverableLeve("JsonParseException");
                sendToDLQ(record, exception);
                return;
            }

            if (rootCause instanceof SQLException) {
                log.error( "Message non traité à cause d'une exception SQL→ DLQ direct: offset={} exception={}", record.offset(), rootCause.getMessage());
                publierLog.publierExceptionNonRecoverableLeve("SQLException");
                sendToDLQ(record, exception);
                return;
            }

            if (isRecoverable(rootCause)) {
                publierLog.publierExceptionRecoverableLeve(rootCause.getMessage());
                boolean success = retryWithBackoff(record, consumer);
                publierLog.publierExceptionRecoverableLeve("Recovery successful");

                if (!success) {
                    log.warn("Max retries atteint → DLQ: offset={}", record.offset());
                    publierLog.publierExceptionRecoverableLeve(rootCause.getMessage());
                }
                return;
            }

            log.error("Exception non récupérable → DLQ: offset={} rootCauseClass={} rootCauseMsg={}",
                    record.offset(), rootCause.getClass(), rootCause.getMessage());
            publierLog.publierExceptionNonRecoverableLeve("Exception non récupérable");
            sendToDLQ(record, exception);

        } catch (Exception e) {
            log.error("Erreur dans le handler pour offset={}", record.offset(), e);
            publierLog.publierExceptionNonRecoverableLeve("Erreur dans le handler");
            sendToDLQ(record, e);
        }
    }


    @Override
    public <K, V> ConsumerRecords<K, V> handleBatchAndReturnRemaining(
            Exception thrownException,
            ConsumerRecords<?, ?> data,
            Consumer<?, ?> consumer,
            MessageListenerContainer container,
            Runnable invokeListener) {

        ConsumerRecord<?, ?> failedRecord = currentRecord;

        if (failedRecord != null) {
            // Log ou traitement spécifique du record fautif
            handleSingleRecord(failedRecord, thrownException, consumer);

            // Filtrer les records restants après le failedRecord
            List<ConsumerRecord<K, V>> remainingRecords = new ArrayList<>();

            boolean startAdding = false;
            for (ConsumerRecord<?, ?> record : data) {
                if (startAdding) {
                    remainingRecords.add((ConsumerRecord<K, V>) record);
                }
                if (record.offset() == failedRecord.offset() &&
                        record.partition() == failedRecord.partition() &&
                        record.topic().equals(failedRecord.topic())) {
                    startAdding = true; // Commence à ajouter après ce record
                }
            }

            // Créer un ConsumerRecords avec les restants
            if (!remainingRecords.isEmpty()) {
                Map<TopicPartition, List<ConsumerRecord<K, V>>> recordsMap = remainingRecords.stream()
                        .collect(Collectors.groupingBy(
                                r -> new TopicPartition(r.topic(), r.partition())
                        ));

                return new ConsumerRecords<>(recordsMap);
            }
        } else {
            log.error("Impossible d'identifier le record échoué dans le batch");
        }

        // Si aucun restant à rejouer → batch vide
        return ConsumerRecords.empty();
    }


    @Override
    public boolean handleOne(Exception thrownException, ConsumerRecord<?, ?> record, Consumer<?, ?> consumer,
                             MessageListenerContainer container) {

        handleSingleRecord(record, thrownException, consumer);
        return true;
    }


    // --- Helpers ---

    private boolean isRecoverable(Throwable exception) {

        return exception instanceof java.net.SocketTimeoutException;
    }

    private boolean retryWithBackoff(ConsumerRecord<?, ?> record, Consumer<?, ?> consumer) {
        ExponentialBackOff backOff = new ExponentialBackOff(initialInterval, multiplier);
        backOff.setMaxElapsedTime(maxRetries * initialInterval);
        BackOffExecution exec = backOff.start();

        int attempt = 0;
        for (;;) {
            attempt++;
            try {
                log.info("🔁 Retry attempt {} pour offset {}", attempt, record.offset());
                publierLog.publierExceptionRecoverableLeve("🔁 Retry attempt {"+attempt+"} pour offset {"+record.offset()+"}");
                return true; // Si ça passe, on sort
            } catch (Exception e) {
                long interval = exec.nextBackOff();
                if (interval == BackOffExecution.STOP) {
                    return false; // plus de retries
                }
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
    }

    private void sendToDLQ(ConsumerRecord<?, ?> record, Exception exception) {

        publierLog.publierSendToDLQ(record, exception);

        log.error("📦 DLQ: topic={}, partition={}, offset={}, key={}, value={} exception={}",
                record.topic(), record.partition(), record.offset(), record.key(),
                record.value(), exception.getMessage());

        System.out.println("TO DLQ -----exception: "+exception+"------------------------exception Cause: "
                +exception.getCause()+"------------------exception Class: "+exception.getClass()+"----------" +
                "-------------------------");

        kafkaTemplate.send("facture-DLQ", record.value().toString());

    }

    private Throwable getRootCause(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    // --- Impl par défaut ---

    @Override
    public void handleOtherException(Exception thrownException, Consumer<?, ?> consumer,
                                     MessageListenerContainer container, boolean batchListener) {
        log.error("Erreur hors batch (poll/infra)", thrownException);
    }



    @Override
    public boolean isAckAfterHandle() { return false; }

}
