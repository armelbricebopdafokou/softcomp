package com.bricebopda.softcomp.Wago.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bricebopda.softcomp.Wago.dtos.WagoControlDto;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/wago")
public class WagoController {
   
    @PostMapping("control")
    public void wagoControl(@RequestBody WagoControlDto entity) {
        //TODO: process POST request
        
    }
    
   
}
