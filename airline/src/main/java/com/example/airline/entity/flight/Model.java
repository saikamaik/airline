package com.example.airline.entity.flight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Model implements Serializable {
    private String en;
    private String ru;

    public Model(String en, String ru) {
        this.en = en;
        this.ru = ru;
    }

    public Model() {
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

    public String getRu() {
        return ru;
    }

    public void setRu(String ru) {
        this.ru = ru;
    }
}

