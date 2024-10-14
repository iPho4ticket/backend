package com.ipho4ticket.seatservice.infra.messaging;


import com.ipho4ticket.seatservice.application.events.CancelTicketEvent;
import com.ipho4ticket.seatservice.application.events.ConfirmSeatEvent;
import com.ipho4ticket.seatservice.application.events.SeatBookingEvent;
import com.ipho4ticket.seatservice.application.service.SeatService;
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
    private final SeatService seatService;


    public void startListener(String topic, UUID uuid) {
        log.debug("register Listener: {}, {}", topic, uuid);
        String listenerId = topic + "-" + uuid;
        CustomKafkaListenerEndpoint endpoint = new CustomKafkaListenerEndpoint(listenerId, listenerId, this);

        endpointRegistry.registerListenerContainer(endpoint, factory);
    }


    @Async
    @Override
    public void onMessage(ConsumerRecord<String, Object> record) {
        log.debug("Received message: key = {}, value = {}", record.key(), record.value());

        if(record.topic().startsWith("seat-booking")) {
            SeatBookingEvent event = (SeatBookingEvent) record.value();
            seatService.checkSeat(event);
        }else if(record.topic().startsWith("cancel-ticket")) {
            CancelTicketEvent event=(CancelTicketEvent) record.value();
            seatService.updateSeatToAvailable(event);
        }else if(record.topic().startsWith("confirm-seat")){
            ConfirmSeatEvent event=(ConfirmSeatEvent) record.value();
            seatService.updateSeatToSold(event);
        }
    }
}
