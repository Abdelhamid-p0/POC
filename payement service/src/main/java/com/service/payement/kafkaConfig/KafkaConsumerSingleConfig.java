package com.service.payement.kafkaConfig;

import com.service.payement.service.PublierLog;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerSingleConfig {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaConsumerSingleConfig(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Bean
    public ConsumerFactory<String, String> consumerSingleFactory() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-group");
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        return new DefaultKafkaConsumerFactory<>(props);
    }
    @Bean
    public ConsumerErrorHandler consumerSingleErrorHandler(PublierLog publierLog) {
        ConsumerErrorHandler errorHandler =
                new ConsumerErrorHandler(3, 1000L, 2.0, publierLog); // maxRetries=3, initialInterval=1s, multiplier=2
        errorHandler.setKafkaTemplate(kafkaTemplate); // injection du KafkaTemplate
        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerSingleContainerFactory(
            ConsumerErrorHandler consumerErrorHandler
    )
    {

        ConcurrentKafkaListenerContainerFactory<String, String> singleFactory =
                new ConcurrentKafkaListenerContainerFactory<>();

        singleFactory.setConsumerFactory(consumerSingleFactory());
        singleFactory.setBatchListener(false);
        singleFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        singleFactory.setCommonErrorHandler(consumerErrorHandler);

        return singleFactory;
    }


}
