package com.bricebopda.softcomp.Wago.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) représentant une commande ou un statut Wago.
 *
 * Utilisé pour :
 *  1. Désérialiser le payload MQTT entrant (topic Wago750/Status)
 *  2. Transmettre la commande vers WagoService.processWagoControl()
 *
 * Le champ {@code status} est un entier dont chaque bit représente
 * l'état d'une lumière (0 = OFF, 1 = ON).
 *
 * Bug fix : était byte[] — incompatible avec le builder et le type retourné
 * par Short.parseShort() dans convertPayloadToDto().
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WagoControlDto {

    /** Statut des lumières encodé en entier 16 bits */
    private int status;
}
