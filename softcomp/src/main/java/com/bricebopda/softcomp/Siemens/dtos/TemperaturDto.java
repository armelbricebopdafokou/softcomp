package com.bricebopda.softcomp.Siemens.dtos;

import com.bricebopda.softcomp.Siemens.models.DifferenzTemperatur;
import com.bricebopda.softcomp.Siemens.models.IstTemperatur;
import com.bricebopda.softcomp.Siemens.models.SollTemperatur;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * DTO de réponse REST pour les températures du Siemens S7-1500.
 *
 * Utilisé par SiemensController pour retourner les valeurs sous un format
 * unifié au frontend, sans exposer directement les entités MongoDB.
 *
 * Les trois valeurs correspondent aux trois topics MQTT :
 *  - ist       → S7_1500/Temperatur/Ist       (température réelle mesurée)
 *  - soll      → S7_1500/Temperatur/Soll      (température cible consigne)
 *  - differenz → S7_1500/Temperatur/Differenz (écart entre Ist et Soll)
 *
 * Note : les champs peuvent être null si une valeur n'a pas encore été reçue
 * (ex: au démarrage, avant réception du premier message MQTT).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemperaturDto {

    /** Température réelle (Ist-Temperatur) en degrés Celsius */
    private Double ist;

    /** Température cible (Soll-Temperatur) en degrés Celsius */
    private Double soll;

    /** Différence (Ist - Soll) en degrés Celsius */
    private Double differenz;

    /** Horodatage de la dernière mesure reçue */
    private Date timestamp;

    public static TemperaturDto from(IstTemperatur ist) {
        return TemperaturDto.builder()
                .ist(ist.getTemperatur())
                .soll(null)
                .differenz(null)
                .timestamp(ist.getTimestamp())
                .build();
    }

    public static TemperaturDto from(SollTemperatur soll) {
        return TemperaturDto.builder()
                .ist(null)
                .soll(soll.getTemperatur())
                .differenz(null)
                .timestamp(soll.getTimestamp())
                .build();
    }

    public static TemperaturDto from(DifferenzTemperatur differenz) {
        return TemperaturDto.builder()
                .ist(null)
                .soll(null)
                .differenz(differenz.getDifferenz())
                .timestamp(differenz.getTimestamp())
                .build();
    }

        /**
         * Agrège les dernières mesures Ist/Soll/Differenz dans une seule réponse DTO.
         * Les champs absents restent à null.
         */
        public static TemperaturDto aggregate(
            IstTemperatur ist,
            SollTemperatur soll,
            DifferenzTemperatur differenz) {
        return TemperaturDto.builder()
            .ist(ist != null ? ist.getTemperatur() : null)
            .soll(soll != null ? soll.getTemperatur() : null)
            .differenz(differenz != null ? differenz.getDifferenz() : null)
            .timestamp(maxTimestamp(
                ist != null ? ist.getTimestamp() : null,
                soll != null ? soll.getTimestamp() : null,
                differenz != null ? differenz.getTimestamp() : null))
            .build();
        }

        private static Date maxTimestamp(Date... dates) {
        return Arrays.stream(dates)
            .filter(Objects::nonNull)
            .max(Date::compareTo)
            .orElse(null);
        }
}
