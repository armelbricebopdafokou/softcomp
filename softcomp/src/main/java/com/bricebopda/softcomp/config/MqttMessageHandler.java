package com.bricebopda.softcomp.config;

import org.springframework.messaging.Message;

/**
 * Contrat commun pour les services capables de traiter un message MQTT entrant.
 *
 * Chaque implémentation annonce les topics qu'elle supporte via supports(),
 * puis traite le message dans handle().
 */
public interface MqttMessageHandler {

    /**
     * Traite le message MQTT entrant.
     *
     * @param message message Spring Integration avec payload et headers MQTT
     */
    void handle(Message<?> message);

    /**
     * Indique si ce handler doit traiter le topic MQTT fourni.
     *
     * @param topic topic reçu dans le header mqtt_receivedTopic
     * @return true si ce handler supporte ce topic, false sinon
     */
    boolean supports(String topic);
}