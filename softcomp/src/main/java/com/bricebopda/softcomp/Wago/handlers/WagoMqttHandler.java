package com.bricebopda.softcomp.Wago.handlers;

import com.bricebopda.softcomp.Wago.services.WagoService;
import com.bricebopda.softcomp.config.MqttMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Handler MQTT dédié aux topics Wago.
 */
@Component
public class WagoMqttHandler implements MqttMessageHandler {

    private static final String WAGO_TOPIC_PREFIX = "Wago750/";

    private final WagoService wagoService;

    public WagoMqttHandler(WagoService wagoService) {
        this.wagoService = wagoService;
    }

    @Override
    public void handle(Message<?> message) {
        wagoService.handleWagoMessage(message);
    }

    @Override
    public boolean supports(String topic) {
        return topic != null && topic.startsWith(WAGO_TOPIC_PREFIX);
    }
}