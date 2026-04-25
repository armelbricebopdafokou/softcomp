package com.bricebopda.softcomp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.util.stream.Stream;

/**
 * Configuration Spring Integration pour MQTT.
 *
 * Cette classe déclare :
 *  - La factory Paho MQTT avec les options de connexion (URL, credentials, reconnexion auto)
 *  - Un canal entrant (inbound) et un canal sortant (outbound)
 *  - Un adapter inbound qui s'abonne à TOUS les topics (Wago + Siemens)
 *  - Un handler outbound pour publier sur le topic de contrôle Wago
 *
 * Le routage par topic est délégué à {@link MqttMessageRouter}.
 */
@Configuration
@EnableIntegration
public class MqttConfig {

    /** URL du broker MQTT (ex: tcp://v7.ia.hs-bochum.de:1883) */
    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    /** Identifiant unique du client MQTT */
    @Value("${mqtt.client-id}")
    private String clientId;

    /** Nom d'utilisateur MQTT */
    @Value("${mqtt.username}")
    private String username;

    /** Mot de passe MQTT */
    @Value("${mqtt.password}")
    private String password;

    /**
     * Topics Wago : Wago750/Status, Wago750/Control
     * Injectés depuis la propriété mqtt.topics-wago (valeurs séparées par des virgules)
     */
    @Value("${mqtt.topics-wago}")
    private String[] wagoTopics;

    /**
        * Topic Siemens - température réelle.
     */
        @Value("${mqtt.topic.siemens.ist}")
        private String siemensTopicIst;

        /**
        * Topic Siemens - température cible.
        */
        @Value("${mqtt.topic.siemens.soll}")
        private String siemensTopicSoll;

        /**
        * Topic Siemens - différence.
        */
        @Value("${mqtt.topic.siemens.differenz}")
        private String siemensTopicDifferenz;

    /**
     * Topic de contrôle Wago pour le handler outbound.
     * Utilisé comme topic par défaut lors de la publication de commandes.
     */
    @Value("${mqtt.control-topic}")
    private String controlTopic;

    /**
     * Crée la factory de clients Paho MQTT avec les options de connexion configurées.
     * - setCleanSession(true) : pas de session persistante côté broker
     * - setAutomaticReconnect(true) : reconnexion automatique en cas de perte de connexion
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl});
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);

        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(options);
        return factory;
    }

    /**
     * Canal Spring Integration pour les messages MQTT entrants.
     * DirectChannel : transmission synchrone, un seul thread à la fois.
     */
    @Bean
    public MessageChannel mqttInboundChannel() {
        return new DirectChannel();
    }

    /**
     * Canal Spring Integration pour les messages MQTT sortants (commandes de contrôle).
     */
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    /**
     * Adapter inbound MQTT : s'abonne à l'ensemble des topics Wago et Siemens.
     *
     * Les tableaux wagoTopics et siemensTopics sont fusionnés en un seul tableau
     * pour un seul adapteur qui écoute tous les topics simultanément.
     * Le routage par topic est ensuite géré par MqttMessageRouter.
     *
     * @return MessageProducerSupport l'adapteur configuré
     */
    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttInboundAdapter() {
        String[] siemensTopics = {siemensTopicIst, siemensTopicSoll, siemensTopicDifferenz};
        String[] allTopics = Stream.concat(
            Stream.of(wagoTopics),
            Stream.of(siemensTopics)
        ).toArray(String[]::new);

        MqttPahoMessageDrivenChannelAdapter adapter =
            new MqttPahoMessageDrivenChannelAdapter(
                clientId + "-sub", mqttClientFactory(), allTopics);
        adapter.setCompletionTimeout(5000);
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInboundChannel());

       
        return adapter;
    }

    /**
     * Handler outbound MQTT : publie les messages sur le topic de contrôle Wago.
     *
     * Utilise le controlTopic comme topic par défaut (Wago750/Control).
     * Le topic peut être surchargé via le header MqttHeaders.TOPIC dans le message.
     * setAsync(true) : publication non bloquante.
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutboundAdapter() {
        MqttPahoMessageHandler handler =
                new MqttPahoMessageHandler(clientId + "-pub", mqttClientFactory());
        handler.setAsync(true);
        handler.setDefaultTopic(controlTopic); // Bug fix: était topics[1] hardcodé
        handler.setDefaultQos(1);
        return handler;
    }
}