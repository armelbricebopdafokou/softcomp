package com.bricebopda.softcomp.Wago.services;

import org.springframework.stereotype.Service;

import com.bricebopda.softcomp.Wago.dtos.WagoControlDto;
import com.bricebopda.softcomp.Wago.repositories.IWagoRepository;

@Service
public class WagoService {

    private final IWagoRepository wagoRepository;

    public WagoService(IWagoRepository wagoRepository) {
        this.wagoRepository = wagoRepository;
    }

    public void processWagoControl(WagoControlDto status) {
        //TODO: Implement Wago control processing logic 
    }
}
