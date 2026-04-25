package com.bricebopda.softcomp.Siemens.repositories;

import com.bricebopda.softcomp.Siemens.models.IstTemperatur;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository MongoDB pour la température actuelle (Ist-Temperatur) du Siemens S7-1500.
 *
 * Spring Data génère automatiquement l'implémentation de findTopByOrderByTimestampDesc()
 * à partir de la convention de nommage : "findTop" + "By" + "OrderBy" + "Timestamp" + "Desc".
 */
public interface IIstTemperaturRepository extends MongoRepository<IstTemperatur, String> {

    /**
     * Retourne la mesure Ist-Temperatur la plus récente.
     * Utilisé par GET /api/siemens/ist/latest.
     *
     * @return la dernière IstTemperatur enregistrée, ou null si la collection est vide
     */
    IstTemperatur findTopByOrderByTimestampDesc();
}
