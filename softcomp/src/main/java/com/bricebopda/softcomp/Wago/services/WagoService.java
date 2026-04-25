package com.bricebopda.softcomp.Wago.services;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import com.bricebopda.softcomp.Wago.dtos.WagoControlDto;
import com.bricebopda.softcomp.Wago.models.WagoStatus;
import com.bricebopda.softcomp.Wago.repositories.IWagoRepository;

/**
 * Service métier pour l'automate Wago 750.
 *
 * Responsabilités :
 *  1. Réception et traitement des messages MQTT entrants (topic Wago750/Status)
 *     → délégués par MqttMessageRouter via handleWagoMessage()
 *  2. Persistence des statuts reçus dans MongoDB
 *  3. Consultation des statuts (dernier + historique) pour les endpoints REST
 *  4. Publication de commandes de contrôle sur le topic Wago750/Control
 *
 * Note : le @ServiceActivator a été retiré de cette classe.
 * Il se trouve maintenant dans MqttMessageRouter pour éviter les conflits
 * de handlers multiples sur le canal mqttInboundChannel.
 */
@Service
public class WagoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WagoService.class);

    private final IWagoRepository wagoRepository;

    /** Canal Spring Integration connecté au handler MQTT outbound */
    private final MessageChannel mqttOutboundChannel;

    /**
     * Injection par constructeur du repository MongoDB.
     * Le mqttOutboundChannel est injecté par @Autowired car il crée une dépendance circulaire
     * potentielle si passé par constructeur avec la config MQTT.
     */
   // WagoService.java — remplacer le field @Autowired par ceci

    public WagoService(IWagoRepository wagoRepository,
                    @Lazy @Qualifier("mqttOutboundChannel") MessageChannel mqttOutboundChannel) {
        this.wagoRepository = wagoRepository;
        this.mqttOutboundChannel = mqttOutboundChannel;
    }

    // =========================================================================
    // MQTT Inbound — appelé par MqttMessageRouter
    // =========================================================================

    /**
     * Traite un message MQTT entrant depuis le topic Wago750/Status.
     *
     * Flux : payload (String) → WagoControlDto → WagoStatus → MongoDB
     *
     * Le payload est un entier représentant l'état binaire des 16 lumières.
     * Exemple : payload "3" = 0b0000000000000011 → lumières 1 et 2 allumées.
     *
     * @param message Message Spring Integration avec header mqtt_receivedTopic et payload texte
     */
    public void handleWagoMessage(Message<?> message) {
        String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC, String.class);
        Object rawPayload = message.getPayload();

        // Conversion du payload en String (byte[] ou String selon la config broker)
        String payload = rawPayload instanceof byte[]
                ? new String((byte[]) rawPayload, StandardCharsets.UTF_8)
                : rawPayload.toString();

        LOGGER.info("Message Wago reçu — topic={} payload={}", topic, payload);

        // Seul le topic Status déclenche une sauvegarde ; Control est envoyé par nous
        if ("Wago750/Status".equals(topic)) {
            try {
                WagoControlDto dto = parsePayload(payload);
                persistStatus(dto);
            } catch (NumberFormatException ex) {
                LOGGER.error("Payload Wago invalide (attendu entier) — payload='{}' : {}", payload, ex.getMessage());
            } catch (Exception ex) {
                LOGGER.error("Erreur lors du traitement du message Wago — topic={} payload={}", topic, payload, ex);
            }
        } else {
            LOGGER.debug("Topic Wago ignoré (non Status) : {}", topic);
        }
    }

    // =========================================================================
    // Persistence interne
    // =========================================================================

    /**
     * Construit et sauvegarde un WagoStatus en base MongoDB.
     * L'horodatage est assigné à l'instant de réception (côté backend).
     *
     * @param dto DTO contenant le statut binaire des lumières
     */
    public void persistStatus(WagoControlDto dto) {
        WagoStatus wagoStatus = WagoStatus.builder()
                .status(dto.getStatus())
                .timestamp(new Date())
                .build();
        wagoRepository.save(wagoStatus);
        LOGGER.info("WagoStatus sauvegardé — status={} (binaire: {})",
                dto.getStatus(), Integer.toBinaryString(dto.getStatus()));
    }

    // =========================================================================
    // Requêtes REST — consultation
    // =========================================================================

    /**
     * Retourne le statut Wago le plus récent enregistré en base.
     * Utilisé par GET /api/wago/latest.
     *
     * @return le dernier WagoStatus, ou null si la collection est vide
     */
    public WagoStatus getLatestStatus() {
        return wagoRepository.findTopByOrderByTimestampDesc();
    }

    /**
     * Retourne l'ensemble des statuts Wago stockés en base, triés par MongoDB (_id).
     * Utilisé par GET /api/wago/history.
     *
     * @return liste complète (peut être vide)
     */
    public List<WagoStatus> getAllStatuses() {
        return wagoRepository.findAll();
    }

    // =========================================================================
    // MQTT Outbound — commandes de contrôle
    // =========================================================================

    /**
     * Publie une commande sur le topic Wago750/Control via MQTT.
     *
     * La commande est une chaîne représentant l'état souhaité des lumières
     * (ex: "1" pour allumer la lumière 1, "3" pour allumer les lumières 1 et 2).
     * C'est le frontend qui calcule la valeur entière à envoyer.
     *
     * @param command La commande en plain text à publier sur Wago750/Control
     */
    public void sendControl(String command) {
        Message<String> message = MessageBuilder
                .withPayload(command)
                .setHeader(MqttHeaders.TOPIC, "Wago750/Control")
                .setHeader(MqttHeaders.QOS, 1)
                .build();

        mqttOutboundChannel.send(message);
        LOGGER.info("Commande publiée sur Wago750/Control : {}", command);
    }

    // =========================================================================
    // Méthodes privées utilitaires
    // =========================================================================

    /**
     * Parse le payload MQTT texte en WagoControlDto.
     *
     * Le broker peut envoyer le statut sous forme de nombre entier simple ("42")
     * ou avec des espaces/crochets autour. On nettoie la chaîne avant le parsing.
     *
     * Bug fix : utilisait Short.parseShort() → int, ce qui causait une erreur
     * de type avec le builder (byte[] attendu, short fourni).
     *
     * @param payload La chaîne de caractères brute reçue via MQTT
     * @return WagoControlDto avec le statut parsé
     * @throws NumberFormatException si le payload n'est pas un entier valide
     */
    private WagoControlDto parsePayload(String payload) {
        // Nettoyage des caractères parasites potentiels (crochets, espaces)
        String cleaned = payload.replace("[", "").replace("]", "").trim();
        int status = Integer.parseInt(cleaned);

        return WagoControlDto.builder()
                .status(status)
                .build();
    }
}