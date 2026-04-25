package com.bricebopda.softcomp.Wago.controllers;

import com.bricebopda.softcomp.Wago.dtos.WagoStatusDto;
import com.bricebopda.softcomp.Wago.models.WagoStatus;
import com.bricebopda.softcomp.Wago.services.WagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour l'automate Wago 750.
 *
 * Expose trois endpoints :
 *  - GET  /api/wago/latest   → dernier statut enregistré
 *  - GET  /api/wago/history  → historique complet des statuts
 *  - POST /api/wago/control  → envoi d'une commande de contrôle via MQTT
 *
 * Les données de statut contiennent le champ {@code status} (entier)
 * que le frontend doit convertir en représentation binaire pour afficher
 * l'état individuel de chaque lumière.
 */
@RestController
@RequestMapping("/api/wago")
public class WagoController {

    private final WagoService wagoService;

    /** Injection par constructeur — recommandé pour l'immuabilité et la testabilité */
    public WagoController(WagoService wagoService) {
        this.wagoService = wagoService;
    }

    /**
     * Retourne le dernier statut Wago enregistré en base MongoDB.
     *
     * Exemple de réponse :
     * <pre>
     * {
     *   "rawStatus": 5,
     *   "lights": [true, false, true, false, ...],
     *   "timestamp": "2024-..."
     * }
     * </pre>
     *
     * @return 200 OK avec le WagoStatusDto le plus récent,
     *         ou 204 No Content si aucun statut n'est encore enregistré
     */
    @GetMapping("/latest")
    public ResponseEntity<WagoStatusDto> getLatestStatus() {
        WagoStatus latest = wagoService.getLatestStatus();
        if (latest == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(WagoStatusDto.from(latest));
    }

    /**
     * Retourne l'historique complet des statuts Wago enregistrés.
     *
     * @return 200 OK avec la liste de tous les WagoStatusDto (peut être vide)
     */
    @GetMapping("/history")
    public ResponseEntity<List<WagoStatusDto>> getHistory() {
        List<WagoStatusDto> history = wagoService.getAllStatuses().stream()
                .map(WagoStatusDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(history);
    }

    /**
     * Envoie une commande de contrôle à l'automate Wago via MQTT (topic Wago750/Control).
     *
     * Le body doit être un entier en plain/text représentant l'état souhaité des lumières.
     * Exemple : "3" → allume les lumières 1 et 2 (bits 0 et 1 à 1).
     *
     * @param command La commande à envoyer (plain text, ex: "1", "3", "255")
     * @return 200 OK avec confirmation de l'envoi
     */
    @PostMapping("/control")
    public ResponseEntity<String> sendControl(@RequestBody String command) {
        wagoService.sendControl(command.trim());
        return ResponseEntity.ok("Commande envoyée sur Wago750/Control : " + command.trim());
    }
}
