package com.bricebopda.softcomp.Siemens.handlers;

import com.bricebopda.softcomp.Siemens.services.SiemensService;
import com.bricebopda.softcomp.config.MqttMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Handler MQTT dédié aux topics Siemens.
 */
@Component
public class SiemensMqttHandler implements MqttMessageHandler {

    private static final String SIEMENS_TOPIC_PREFIX = "S7_1500/";

    private final SiemensService siemensService;

    public SiemensMqttHandler(SiemensService siemensService) {
        this.siemensService = siemensService;
    }

    @Override
    public void handle(Message<?> message) {
        siemensService.handleSiemensMessage(message);
    }

    @Override
    public boolean supports(String topic) {
        return topic != null && topic.startsWith(SIEMENS_TOPIC_PREFIX);
    }
}