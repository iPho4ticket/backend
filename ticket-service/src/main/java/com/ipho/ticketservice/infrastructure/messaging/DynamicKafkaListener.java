package com.ipho.ticketservice.infrastructure.messaging;


import com.ipho.common.dto.TicketMakingEvent;
import com.ipho.ticketservice.application.event.TicketTopic;
import com.ipho.ticketservice.application.service.TicketService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "dynamic-kafka-listener")
public class DynamicKafkaListener implements MessageListener<String, Object> {

    private final KafkaListenerEndpointRegistry endpointRegistry;
    private final ConcurrentKafkaListenerContainerFactory<String, String> factory;
    private final TicketService ticketService;

    @Getter
    private Object receivedMessage;

    public void startListener(String topic, UUID uuid) {

        log.debug("register Listener: {}, {}", topic, uuid);
        String listenerId = topic + "-" + uuid;
        CustomKafkaListenerEndpoint endpoint = new CustomKafkaListenerEndpoint(listenerId, listenerId, this);

        endpointRegistry.registerListenerContainer(endpoint, factory);
        endpointRegistry.getListenerContainer(listenerId).start();
    }

    @Override
    @Async
    public void onMessage(ConsumerRecord<String, Object> record) {
        log.debug("Received message: key = {}, value = {}", record.key(), record.value());

        log.debug("record.topic() = {}", record.topic());
        log.debug("record.value() = {}", record.value());
        this.receivedMessage = record.value();

        if (record.topic().startsWith(TicketTopic.TICKET_MAKING.getTopic())) {

            TicketMakingEvent event = (TicketMakingEvent) record.value();
            ticketService.handleTicketMaking(event);
        }
    }
}
