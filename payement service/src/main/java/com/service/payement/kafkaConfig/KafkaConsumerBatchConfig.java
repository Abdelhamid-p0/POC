package com.service.payement.kafkaConfig;

import com.service.payement.dashboardApi.controller.DashboardController;
import com.service.payement.dashboardApi.service.DashboardService;
import com.service.payement.service.PublierLog;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;


import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerBatchConfig {


    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaConsumerBatchConfig(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Bean
    public ConsumerFactory<String, String> consumerBatchFactory() {
       Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-group");
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    /*
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, String> template) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
                (record, ex) -> new TopicPartition("facture-DLQ",
                        record.partition()));

        FixedBackOff backOff = new FixedBackOff(2000L, 3L);

        return new DefaultErrorHandler(recoverer, backOff);
    }
    */


    @Bean
    public ConsumerErrorHandler consumerErrorHandler(PublierLog publierLog) {
        ConsumerErrorHandler errorHandler =
                new ConsumerErrorHandler(3, 1000L, 2.0,publierLog); // maxRetries=3, initialInterval=1s, multiplier=2
        errorHandler.setKafkaTemplate(kafkaTemplate); // injection du KafkaTemplate
        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerBatchContainerFactory(
            ConsumerErrorHandler consumerErrorHandler)
    {

        ConcurrentKafkaListenerContainerFactory<String, String> batchFactory =
                new ConcurrentKafkaListenerContainerFactory<>();

        batchFactory.setConsumerFactory(consumerBatchFactory());
        batchFactory.setBatchListener(true);
        batchFactory.setCommonErrorHandler(consumerErrorHandler);
        batchFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);


        return batchFactory;
    }
}
