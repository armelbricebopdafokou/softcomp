package com.bricebopda.softcomp.Wago.repositories;

import com.bricebopda.softcomp.Wago.models.WagoStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository MongoDB pour l'entité {@link WagoStatus}.
 *
 * Hérite de MongoRepository qui fournit les opérations CRUD standard :
 * save(), findAll(), findById(), deleteById(), etc.
 *
 * La méthode {@code findTopByOrderByTimestampDesc()} utilise la convention
 * de nommage Spring Data pour générer automatiquement la requête :
 * "trouve le premier document trié par timestamp décroissant" → dernier enregistré.
 */
public interface IWagoRepository extends MongoRepository<WagoStatus, String> {

    /**
     * Retourne le statut Wago le plus récent enregistré en base.
     * Utilisé par le endpoint GET /api/wago/latest.
     *
     * @return le WagoStatus avec le timestamp le plus récent, ou null si la collection est vide
     */
    WagoStatus findTopByOrderByTimestampDesc();
}
