package com.bricebopda.softcomp.Siemens.repositories;

import com.bricebopda.softcomp.Siemens.models.DifferenzTemperatur;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository MongoDB pour la différence de température (Differenz-Temperatur) du Siemens S7-1500.
 */
public interface IDiffTemperaturRepository extends MongoRepository<DifferenzTemperatur, String> {

    /**
     * Retourne la mesure Differenz-Temperatur la plus récente.
     * Utilisé par GET /api/siemens/differenz/latest.
     *
     * @return la dernière DifferenzTemperatur enregistrée, ou null si la collection est vide
     */
    DifferenzTemperatur findTopByOrderByTimestampDesc();
}
