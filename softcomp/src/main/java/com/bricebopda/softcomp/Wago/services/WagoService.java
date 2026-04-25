package com.bricebopda.softcomp.Wago.services;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import com.bricebopda.softcomp.Wago.dtos.WagoControlDto;
import com.bricebopda.softcomp.Wago.models.WagoStatus;
import com.bricebopda.softcomp.Wago.repositories.IWagoRepository;


@Service
public class WagoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WagoService.class);

    private final IWagoRepository wagoRepository;

    @Autowired
    @Qualifier("mqttOutboundChannel")
    private MessageChannel mqttOutboundChannel;

    public WagoService(IWagoRepository wagoRepository) {
        this.wagoRepository = wagoRepository;
    }

    // Appelé automatiquement à chaque message reçu sur le topic
    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public void handleInboundMessage(Message<?> message) {
        String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC, String.class);
        Object rawPayload = message.getPayload();
        String payload = rawPayload instanceof byte[]
                ? new String((byte[]) rawPayload, StandardCharsets.UTF_8)
                : rawPayload.toString();

        LOGGER.info("Message reçu sur topic={} payload={}", topic, payload);

        try {
            WagoControlDto dto = convertPayloadToDto(payload);
            processWagoControl(dto);
        } catch (Exception ex) {
            LOGGER.warn("Impossible de parser le payload topic={} payload={}", topic, payload, ex);
        }
    }

    public void processWagoControl(WagoControlDto control) {
        WagoStatus wagoStatus = WagoStatus.builder()
                .status(control.getStatus())
                .timestamp(new Date())
                .build();
        wagoRepository.save(wagoStatus);
        LOGGER.info("WagoStatus sauvegardé avec status={}", control.getStatus());
    }



    private WagoControlDto convertPayloadToDto(String payload) {
        short status = Short.parseShort(payload.replace("[", "").replace("]", "").trim());

        return WagoControlDto.builder()
                .status(status)
                .build();
    }

     public void sendControl(String command) {
        Message<String> message = MessageBuilder
                .withPayload(command)
                .setHeader(MqttHeaders.TOPIC, "Wago750/Control")
                .setHeader(MqttHeaders.QOS, 1)
                .build();

        mqttOutboundChannel.send(message);
        LOGGER.info("Commande envoyée sur Wago750/Control : {}", command);
    }


}