package com.example.demo.util;

import java.util.List;

public class StatisticsUtil {

    public static double calcularMedia(List<Double> datos) {
        if (datos == null || datos.isEmpty()) {
            return 0.0;
        }
        return datos.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    public static double calcularDesviacionEstandar(List<Double> datos, double media) {
        if (datos == null || datos.isEmpty() || datos.size() == 1) {
            return 0.0;
        }

        // VARIANZA MUESTRAL (divide entre n-1)
        double varianza = datos.stream()
                .mapToDouble(d -> Math.pow(d - media, 2))
                .sum() / (datos.size() - 1);

        return Math.sqrt(varianza);
    }

    public static double calcularVarianzaMuestral(List<Double> datos, double media) {
        if (datos == null || datos.isEmpty() || datos.size() == 1) {
            return 0.0;
        }
        return datos.stream()
                .mapToDouble(d -> Math.pow(d - media, 2))
                .sum() / (datos.size() - 1);
    }

    public static double calcularVarianzaPoblacional(List<Double> datos, double media) {
        if (datos == null || datos.isEmpty()) {
            return 0.0;
        }
        return datos.stream()
                .mapToDouble(d -> Math.pow(d - media, 2))
                .average()
                .orElse(0.0);
    }

    public static List<Double> normalizarDatos(List<Double> datos, double media, double desviacion) {
        if (desviacion == 0) {
            return datos.stream().map(d -> 0.0).toList();
        }
        return datos.stream()
                .map(d -> (d - media) / desviacion)
                .toList();
    }

    public static double[] calcularLimitesHistograma(List<Double> datos) {
        if (datos == null || datos.isEmpty()) {
            return new double[]{-4.0, 4.0};
        }

        // Calcular percentiles para límites más inteligentes
        double percentil5 = calcularPercentil(datos, 5);
        double percentil95 = calcularPercentil(datos, 95);

        return new double[]{percentil5, percentil95};
    }

    public static double calcularPercentil(List<Double> datos, double percentil) {
        if (datos == null || datos.isEmpty()) {
            return 0.0;
        }

        List<Double> sorted = datos.stream().sorted().toList();
        int index = (int) Math.ceil((percentil / 100.0) * datos.size()) - 1;

        if (index < 0) return sorted.get(0);
        if (index >= datos.size()) return sorted.get(datos.size() - 1);

        return sorted.get(index);
    }

    // Método adicional para análisis de Chebyshev
    public static int contarDatosEnIntervalo(List<Double> datos, double limiteInferior, double limiteSuperior) {
        if (datos == null || datos.isEmpty()) {
            return 0;
        }
        return (int) datos.stream()
                .filter(d -> d >= limiteInferior && d <= limiteSuperior)
                .count();
    }

    // Método para calcular el rango intercuartílico (opcional)
    public static double calcularRangoIntercuartilico(List<Double> datos) {
        if (datos == null || datos.size() < 4) {
            return 0.0;
        }
        double q1 = calcularPercentil(datos, 25);
        double q3 = calcularPercentil(datos, 75);
        return q3 - q1;
    }
}