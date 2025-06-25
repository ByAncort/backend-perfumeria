package org.necronet.mslogistica.model;


import lombok.Data;

@Data
public class RutaOptimizada {
    private String origen;
    private String destino;
    private String rutaRecomendada;
    private double distancia; // en kilómetros
    private double tiempoEstimado; // en horas
    private String transportista;
}