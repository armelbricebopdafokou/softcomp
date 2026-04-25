package com.bricebopda.softcomp.Siemens.controllers;

import com.bricebopda.softcomp.Siemens.dtos.TemperaturDto;
import com.bricebopda.softcomp.Siemens.models.DifferenzTemperatur;
import com.bricebopda.softcomp.Siemens.models.IstTemperatur;
import com.bricebopda.softcomp.Siemens.models.SollTemperatur;
import com.bricebopda.softcomp.Siemens.services.SiemensService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour l'automate Siemens S7-1500.
 *
 * Expose 6 endpoints de lecture (lecture seule — le Siemens ne reçoit pas de commandes
 * via cette API dans les exigences du PoC) :
 *
 * Vue agrégée :
 *  - GET /api/siemens/latest           → dernières valeurs Ist + Soll + Differenz
 *
 * Dernières valeurs :
 *  - GET /api/siemens/ist/latest       → dernière température actuelle
 *  - GET /api/siemens/soll/latest      → dernière température cible
 *  - GET /api/siemens/differenz/latest → dernière différence
 *
 * Historiques complets :
 *  - GET /api/siemens/ist/history       → toutes les mesures Ist
 *  - GET /api/siemens/soll/history      → toutes les mesures Soll
 *  - GET /api/siemens/differenz/history → toutes les différences
 */
@RestController
@RequestMapping("/api/siemens")
public class SiemensController {

    private final SiemensService siemensService;

    /** Injection par constructeur */
    public SiemensController(SiemensService siemensService) {
        this.siemensService = siemensService;
    }

    /**
     * Retourne une vue agrégée des dernières valeurs Ist, Soll et Differenz.
     *
     * @return 200 OK avec le DTO agrégé, ou 204 No Content si aucune donnée n'existe
     */
    @GetMapping("/latest")
    public ResponseEntity<TemperaturDto> getLatest() {
        IstTemperatur ist = siemensService.getLatestIst();
        SollTemperatur soll = siemensService.getLatestSoll();
        DifferenzTemperatur differenz = siemensService.getLatestDifferenz();

        if (ist == null && soll == null && differenz == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(TemperaturDto.aggregate(ist, soll, differenz));
    }

    // =========================================================================
    // Dernières valeurs
    // =========================================================================

    /**
     * Retourne la dernière Ist-Temperatur (température réelle mesurée).
     *
     * Exemple de réponse :
     * <pre>{ "ist": 22.5, "soll": null, "differenz": null, "timestamp": "2024-..." }</pre>
     *
     * @return 200 OK avec la mesure, ou 204 No Content si aucune donnée disponible
     */
    @GetMapping("/ist/latest")
    public ResponseEntity<TemperaturDto> getLatestIst() {
        IstTemperatur ist = siemensService.getLatestIst();
        if (ist == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(TemperaturDto.from(ist));
    }

    /**
     * Retourne la dernière Soll-Temperatur (température cible / consigne).
     *
     * @return 200 OK avec la mesure, ou 204 No Content si aucune donnée disponible
     */
    @GetMapping("/soll/latest")
    public ResponseEntity<TemperaturDto> getLatestSoll() {
        SollTemperatur soll = siemensService.getLatestSoll();
        if (soll == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(TemperaturDto.from(soll));
    }

    /**
     * Retourne la dernière Differenz-Temperatur (écart entre Ist et Soll).
     *
     * @return 200 OK avec la mesure, ou 204 No Content si aucune donnée disponible
     */
    @GetMapping("/differenz/latest")
    public ResponseEntity<TemperaturDto> getLatestDifferenz() {
        DifferenzTemperatur differenz = siemensService.getLatestDifferenz();
        if (differenz == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(TemperaturDto.from(differenz));
    }

    // =========================================================================
    // Historiques complets
    // =========================================================================

    /**
     * Retourne l'historique complet des mesures Ist-Temperatur.
     * Requis par le PDF (Aufgabe 3 : Abfrage aller Temperaturwerte aus der DB).
     *
     * @return 200 OK avec la liste (peut être un tableau vide [])
     */
    @GetMapping("/ist/history")
    public ResponseEntity<List<TemperaturDto>> getIstHistory() {
        List<TemperaturDto> history = siemensService.getAllIst().stream()
                .map(TemperaturDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(history);
    }

    /**
     * Retourne l'historique complet des mesures Soll-Temperatur.
     *
     * @return 200 OK avec la liste (peut être un tableau vide [])
     */
    @GetMapping("/soll/history")
    public ResponseEntity<List<TemperaturDto>> getSollHistory() {
        List<TemperaturDto> history = siemensService.getAllSoll().stream()
                .map(TemperaturDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(history);
    }

    /**
     * Retourne l'historique complet des mesures Differenz-Temperatur.
     *
     * @return 200 OK avec la liste (peut être un tableau vide [])
     */
    @GetMapping("/differenz/history")
    public ResponseEntity<List<TemperaturDto>> getDifferenzHistory() {
        List<TemperaturDto> history = siemensService.getAllDifferenz().stream()
                .map(TemperaturDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(history);
    }
}
