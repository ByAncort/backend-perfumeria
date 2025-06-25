package org.necronet.mslogistica.dto;


import lombok.Data;

@Data
public class RutaOptimizadaDto {
    private String origen;
    private String destino;
    private String rutaRecomendada;
    private double distancia;
    private double tiempoEstimado;
    private String transportista;
}