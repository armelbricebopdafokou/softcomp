package com.bricebopda.softcomp.Siemens.services;

import com.bricebopda.softcomp.Siemens.models.DifferenzTemperatur;
import com.bricebopda.softcomp.Siemens.models.IstTemperatur;
import com.bricebopda.softcomp.Siemens.models.SollTemperatur;
import com.bricebopda.softcomp.Siemens.repositories.IDiffTemperaturRepository;
import com.bricebopda.softcomp.Siemens.repositories.IIstTemperaturRepository;
import com.bricebopda.softcomp.Siemens.repositories.ISollTemperaturRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Service métier pour l'automate Siemens S7-1500.
 *
 * Responsabilités :
 *  1. Réception et traitement des messages MQTT Siemens (topics S7_1500/Temperatur/*)
 *     → délégués par MqttMessageRouter via handleSiemensMessage()
 *  2. Discrimination par sous-topic (Ist / Soll / Differenz) et persistence dans MongoDB
 *  3. Consultation des dernières valeurs et de l'historique pour les endpoints REST
 *
 * Architecture des topics Siemens :
 *  - S7_1500/Temperatur/Ist       → IstTemperatur (collection "Ist_Temperatur")
 *  - S7_1500/Temperatur/Soll      → SollTemperatur (collection "soll_Temperatur")
 *  - S7_1500/Temperatur/Differenz → DifferenzTemperatur (collection "differenz_Temperatur")
 */
@Service
public class SiemensService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SiemensService.class);

    // Topics Siemens injectés depuis application.properties via le constructeur
    private final String topicIst;
    private final String topicSoll;
    private final String topicDifferenz;

    private final IIstTemperaturRepository istRepository;
    private final ISollTemperaturRepository sollRepository;
    private final IDiffTemperaturRepository differenzRepository;

    /**
     * Injection par constructeur — topics lus depuis application.properties via @Value,
     * repositories MongoDB injectés par Spring (même pattern que WagoService).
     */
    public SiemensService(
            @Value("${mqtt.topic.siemens.ist}")       String topicIst,
            @Value("${mqtt.topic.siemens.soll}")      String topicSoll,
            @Value("${mqtt.topic.siemens.differenz}") String topicDifferenz,
            IIstTemperaturRepository istRepository,
            ISollTemperaturRepository sollRepository,
            IDiffTemperaturRepository differenzRepository) {
        this.topicIst        = topicIst;
        this.topicSoll       = topicSoll;
        this.topicDifferenz  = topicDifferenz;
        this.istRepository   = istRepository;
        this.sollRepository  = sollRepository;
        this.differenzRepository = differenzRepository;
    }

    // =========================================================================
    // MQTT Inbound — appelé par MqttMessageRouter
    // =========================================================================

    /**
     * Traite un message MQTT entrant depuis un topic Siemens (S7_1500/Temperatur/*).
     *
     * Le topic est utilisé pour déterminer quelle entité créer et dans quelle
     * collection MongoDB persister la valeur reçue.
     *
     * Flux : payload (String double) → entité correspondante → MongoDB
     *
     * @param message Message Spring Integration avec header mqtt_receivedTopic et payload double texte
     */
    public void handleSiemensMessage(Message<?> message) {
        String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC, String.class);
        Object rawPayload = message.getPayload();

        // Conversion du payload en String (byte[] ou String selon la config broker)
        String payload = rawPayload instanceof byte[]
                ? new String((byte[]) rawPayload, StandardCharsets.UTF_8)
                : rawPayload.toString();

        LOGGER.info("Message Siemens reçu — topic={} payload={}", topic, payload);

        try {
            double valeur = Double.parseDouble(payload.trim());
            Date now = new Date();

            // Routage interne selon le sous-topic (valeurs lues depuis application.properties)
            if (topicIst.equals(topic)) {
                IstTemperatur ist = IstTemperatur.builder()
                        .temperatur(valeur)
                        .timestamp(now)
                        .build();
                istRepository.save(ist);
                LOGGER.info("IstTemperatur sauvegardée : {}°C", valeur);
            } else if (topicSoll.equals(topic)) {
                SollTemperatur soll = SollTemperatur.builder()
                        .temperatur(valeur)
                        .timestamp(now)
                        .build();
                sollRepository.save(soll);
                LOGGER.info("SollTemperatur sauvegardée : {}°C", valeur);
            } else if (topicDifferenz.equals(topic)) {
                DifferenzTemperatur differenz = DifferenzTemperatur.builder()
                        .differenz(valeur)
                        .timestamp(now)
                        .build();
                differenzRepository.save(differenz);
                LOGGER.info("DifferenzTemperatur sauvegardée : {}°C", valeur);
            } else {
                LOGGER.warn("Sous-topic Siemens non reconnu : {}", topic);
            }
        } catch (NumberFormatException ex) {
            LOGGER.error("Payload Siemens invalide (attendu double) — topic={} payload='{}' : {}",
                    topic, payload, ex.getMessage());
        } catch (Exception ex) {
            LOGGER.error("Erreur lors du traitement du message Siemens — topic={}", topic, ex);
        }
    }

    // =========================================================================
    // Requêtes REST — dernière valeur par type
    // =========================================================================

    /**
     * Retourne la mesure Ist-Temperatur la plus récente.
     * Utilisé par GET /api/siemens/ist/latest.
     *
     * @return la dernière IstTemperatur, ou null si aucune mesure en base
     */
    public IstTemperatur getLatestIst() {
        return istRepository.findTopByOrderByTimestampDesc();
    }

    /**
     * Retourne la mesure Soll-Temperatur la plus récente.
     * Utilisé par GET /api/siemens/soll/latest.
     *
     * @return la dernière SollTemperatur, ou null si aucune mesure en base
     */
    public SollTemperatur getLatestSoll() {
        return sollRepository.findTopByOrderByTimestampDesc();
    }

    /**
     * Retourne la mesure Differenz-Temperatur la plus récente.
     * Utilisé par GET /api/siemens/differenz/latest.
     *
     * @return la dernière DifferenzTemperatur, ou null si aucune mesure en base
     */
    public DifferenzTemperatur getLatestDifferenz() {
        return differenzRepository.findTopByOrderByTimestampDesc();
    }

    // =========================================================================
    // Requêtes REST — historiques complets
    // =========================================================================

    /**
     * Retourne l'historique complet des mesures Ist-Temperatur.
     * Utilisé par GET /api/siemens/ist/history.
     *
     * @return liste de toutes les IstTemperatur enregistrées (peut être vide)
     */
    public List<IstTemperatur> getAllIst() {
        return istRepository.findAll();
    }

    /**
     * Retourne l'historique complet des mesures Soll-Temperatur.
     * Utilisé par GET /api/siemens/soll/history.
     *
     * @return liste de toutes les SollTemperatur enregistrées (peut être vide)
     */
    public List<SollTemperatur> getAllSoll() {
        return sollRepository.findAll();
    }

    /**
     * Retourne l'historique complet des mesures Differenz-Temperatur.
     * Utilisé par GET /api/siemens/differenz/history.
     *
     * @return liste de toutes les DifferenzTemperatur enregistrées (peut être vide)
     */
    public List<DifferenzTemperatur> getAllDifferenz() {
        return differenzRepository.findAll();
    }
}
