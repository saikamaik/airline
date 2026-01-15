package com.example.airline.entity.flight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "aircrafts_data")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Aircraft {
    @Id
    @Column(name = "aircraft_code")
    private String aircraftCode;
    @Column(name = "model", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Model model;
    @Column(name = "range")
    private int range;

    public Aircraft(String aircraftCode, Model model, int range) {
        this.aircraftCode = aircraftCode;
        this.model = model;
        this.range = range;
    }

    public Aircraft() {
    }

    public String getAircraftCode() {
        return aircraftCode;
    }

    public void setAircraftCode(String aircraftCode) {
        this.aircraftCode = aircraftCode;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }
}

