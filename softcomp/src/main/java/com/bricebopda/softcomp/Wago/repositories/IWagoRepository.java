package com.bricebopda.softcomp.Wago.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.bricebopda.softcomp.Wago.models.WagoStatus;

public interface IWagoRepository extends  MongoRepository<WagoStatus, String> {

}
