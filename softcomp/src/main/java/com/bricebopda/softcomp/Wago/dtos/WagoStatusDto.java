package com.bricebopda.softcomp.Wago.dtos;

import java.util.Date;

import com.bricebopda.softcomp.Wago.models.WagoStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class WagoStatusDto {
    private int rawStatus;
    private boolean[] lights; // lights[0] = bit 0, lights[8] = true si status=256
    private Date timestamp;
    
    public static WagoStatusDto from(WagoStatus entity) {
        boolean[] lights = new boolean[16];
        for (int i = 0; i < 16; i++) {
            lights[i] = (entity.getStatus() & (1 << i)) != 0;
        }
        return new WagoStatusDto(entity.getStatus(), lights, entity.getTimestamp());
    }
}