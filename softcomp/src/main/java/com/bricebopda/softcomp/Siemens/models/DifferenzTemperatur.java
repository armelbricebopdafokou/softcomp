package com.bricebopda.softcomp.Siemens.models;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(value = "differenz_Temperatur")
@AllArgsConstructor // This annotation generates a constructor with parameters for all fields in the class
@NoArgsConstructor
@Builder // This annotation provides a builder pattern implementation for the class, allowing for more flexible object creation
@Data // This annotation generates getters, setters, toString, equals, and hashCode methods for the class
public class DifferenzTemperatur {
    private @Id String id;
    private double differenz;
    private Date timestamp;
}

