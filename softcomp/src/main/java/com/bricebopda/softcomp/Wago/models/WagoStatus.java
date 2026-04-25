package com.bricebopda.softcomp.Wago.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Document(value = "wago_status")
@AllArgsConstructor // This annotation generates a constructor with parameters for all fields in the class
@NoArgsConstructor
@Builder // This annotation provides a builder pattern implementation for the class, allowing for more flexible object creation
@Data // This annotation generates getters, setters, toString, equals, and hashCode methods for the class
public class WagoStatus {
    private @Id String id;
    private int status;
    private Date timestamp;
}
