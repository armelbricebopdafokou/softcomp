package com.bricebopda.softcomp.Siemens.repositories;

import com.bricebopda.softcomp.Siemens.models.SollTemperatur;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository MongoDB pour la température cible (Soll-Temperatur) du Siemens S7-1500.
 */
public interface ISollTemperaturRepository extends MongoRepository<SollTemperatur, String> {

    /**
     * Retourne la mesure Soll-Temperatur la plus récente.
     * Utilisé par GET /api/siemens/soll/latest.
     *
     * @return la dernière SollTemperatur enregistrée, ou null si la collection est vide
     */
    SollTemperatur findTopByOrderByTimestampDesc();
}
