package com.example.demo.model;

import lombok.Data;

@Data
public class ResultChebyshev {
    private int k;
    private double limiteInferior;
    private double limiteSuperior;
    private double minimoTeorico;
    private int datosEnIntervalo;
    private double porcentajeReal;
    private boolean cumpleCriterio;
}
