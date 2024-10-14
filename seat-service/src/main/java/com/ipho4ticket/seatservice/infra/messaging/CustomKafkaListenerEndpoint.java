package com.ipho4ticket.seatservice.infra.messaging;

import org.springframework.kafka.config.KafkaListenerEndpoint;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.TopicPartitionOffset;
import org.springframework.kafka.support.converter.MessageConverter;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

public class CustomKafkaListenerEndpoint implements KafkaListenerEndpoint {
    private final String id;
    private final String topic;
    private final DynamicKafkaListener listener;

    public CustomKafkaListenerEndpoint(String id, String topic, DynamicKafkaListener listener) {
        this.id = id;
        this.topic = topic;
        this.listener = listener;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getGroupId() {
        return "dynamic-group";
    }

    @Override
    public String getGroup() {
        return null;
    }

    @Override
    public Collection<String> getTopics() {
        return Collections.singletonList(topic);
    }

    @Override
    public TopicPartitionOffset[] getTopicPartitionsToAssign() {
        return new TopicPartitionOffset[0];
    }

    @Override
    public Pattern getTopicPattern() {
        return null;
    }

    @Override
    public String getClientIdPrefix() {
        return null;
    }

    @Override
    public Integer getConcurrency() {
        return null;
    }

    @Override
    public Boolean getAutoStartup() {
        return null;
    }

    @Override
    public void setupListenerContainer(MessageListenerContainer listenerContainer, MessageConverter messageConverter) {
        listenerContainer.setupMessageListener(listener);
    }

    @Override
    public boolean isSplitIterables() {
        return false;
    }

}