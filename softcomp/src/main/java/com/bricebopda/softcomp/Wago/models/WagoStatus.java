package com.bricebopda.softcomp.Wago.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Entité MongoDB représentant un snapshot du statut des lumières de l'automate Wago 750.
 *
 * Le champ {@code status} est un entier 16 bits envoyé par le Wago via MQTT.
 * Chaque bit représente l'état ON/OFF d'une lumière (bit 0 = lumière 1, etc.).
 * La conversion binaire pour l'affichage frontend est réalisée côté client.
 *
 * Collection MongoDB : "wago_status"
 */
@Document(value = "wago_status")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class WagoStatus {

    /** Identifiant unique généré par MongoDB */
    @Id
    private String id;

    /**
     * Statut des lumières encodé en entier.
     * Bug fix : était byte[] — le Wago envoie un entier (Short/Int) représentant
     * un mot de 16 bits. byte[] empêchait la désérialisation correcte et cassait
     * le builder dans WagoService.
     */
    private int status;

    /** Horodatage d'insertion côté backend (non fourni par le Wago) */
    private Date timestamp;
}
