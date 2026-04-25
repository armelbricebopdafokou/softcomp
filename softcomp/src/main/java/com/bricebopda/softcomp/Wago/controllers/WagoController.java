package com.bricebopda.softcomp.Wago.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bricebopda.softcomp.Wago.dtos.WagoControlDto;
import com.bricebopda.softcomp.Wago.services.WagoService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/wago")
public class WagoController {

    private final WagoService wagoService;

    public WagoController(WagoService wagoService) {
        this.wagoService = wagoService;
    }

    
     // Envoie une commande plain/text vers Wago750/Control
    @PostMapping("/control")
    public ResponseEntity<String> sendControl(@RequestBody String command) {
        wagoService.sendControl(command);
        return ResponseEntity.ok("Commande envoyée : " + command);
    }
   
}
