package com.ipho4ticket.seatservice.infra.messaging;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DynamicKafkaListener implements MessageListener<String, String> {

    private final KafkaListenerEndpointRegistry endpointRegistry;
    private final ConcurrentKafkaListenerContainerFactory<String, String> factory;

    public DynamicKafkaListener(KafkaListenerEndpointRegistry endpointRegistry,
                                ConcurrentKafkaListenerContainerFactory<String, String> factory) {
        this.endpointRegistry = endpointRegistry;
        this.factory = factory;
    }

    public void startListener(String topic, UUID uuid) {
        String listenerId = topic + "-" + uuid;

        CustomKafkaListenerEndpoint endpoint = new CustomKafkaListenerEndpoint(listenerId, listenerId, this);

        endpointRegistry.registerListenerContainer(endpoint, factory);
        endpointRegistry.getListenerContainer(listenerId).start();
    }

    @Override
    @Async
    public void onMessage(ConsumerRecord<String, String> record) {
        System.out.printf("Received message: key = %s, value = %s%n", record.key(), record.value());
    }
}
