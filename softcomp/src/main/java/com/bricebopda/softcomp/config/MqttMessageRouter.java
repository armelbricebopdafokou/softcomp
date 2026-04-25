package com.bricebopda.softcomp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Routeur de messages MQTT entrants.
 *
 * Ce composant est le SEUL @ServiceActivator sur le canal mqttInboundChannel.
 * Il est responsable de dispatcher chaque message vers un handler métier
 * en fonction du topic MQTT reçu dans les headers du message.
 *
 * Architecture de routage générique :
 *  - Chaque bean implémentant {@link MqttMessageHandler} indique s'il supporte le topic
 *    via {@link MqttMessageHandler#supports(String)}
 *  - Le routeur délègue ensuite via {@link MqttMessageHandler#handle(Message)}
 *  - Si aucun handler ne supporte le topic, le message est ignoré avec warning
 *
 * Ce pattern évite les conflits de @ServiceActivator multiples sur le même canal,
 * ce qui causerait une AmbiguousMessageHandlerException au démarrage.
 */
@Component
public class MqttMessageRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttMessageRouter.class);

    private final List<MqttMessageHandler> handlers;

    /**
     * Injection par constructeur des services de traitement métier.
     * Préféré à @Autowired sur champs pour faciliter les tests unitaires.
     */
    public MqttMessageRouter(List<MqttMessageHandler> handlers) {
        this.handlers = handlers;
    }

    /**
     * Point d'entrée unique pour tous les messages MQTT entrants.
     *
     * Lit le header mqtt_receivedTopic (injecté par Spring Integration)
     * et dispatche vers le service correspondant.
     *
     * @param message Le message Spring Integration portant le payload et les headers MQTT
     */
    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public void route(Message<?> message) {
        // Récupération du topic depuis les headers du message
        String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC, String.class);

        if (topic == null) {
            LOGGER.warn("Message MQTT reçu sans topic dans les headers, ignoré.");
            return;
        }

        LOGGER.debug("Message MQTT reçu sur topic: {}", topic);

        handlers.stream()
            .filter(h -> h.supports(topic))
            .findFirst()
            .ifPresentOrElse(
                handler -> {
                    LOGGER.debug("Message MQTT routé vers le handler: {}", handler.getClass().getSimpleName());
                    handler.handle(message);
                },
                () -> LOGGER.warn("Topic MQTT non géré: {}. Message ignoré.", topic)
            );
    }
}
