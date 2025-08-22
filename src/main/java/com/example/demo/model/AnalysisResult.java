package com.example.demo.model;

import lombok.Data;
import java.util.List;

@Data
public class AnalysisResult {
    private double media;
    private double desviacionEstandar;
    private int totalDatos;
    private List<ResultChebyshev> resultadosChebyshev;
    private transient byte[] graficaBytes; // transient para no serializar en JSON

    // Getter para la gráfica en base64 (opcional para frontend)
    public String getGraficaBase64() {
        if (graficaBytes == null) return "";
        return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(graficaBytes);
    }
}
