package com.example.demo.model;

public class GraphicsResponse {
    // Clase interna para la respuesta de múltiples gráficas
        private byte[] histograma;
        private byte[] barras;
        private AnalysisResult resultado;

        // Getters y Setters
        public byte[] getHistograma() {
            return histograma;
        }

        public void setHistograma(byte[] histograma) {
            this.histograma = histograma;
        }

        public byte[] getBarras() {
            return barras;
        }

        public void setBarras(byte[] barras) {
            this.barras = barras;
        }

        public AnalysisResult getResultado() {
            return resultado;
        }

        public void setResultado(AnalysisResult resultado) {
            this.resultado = resultado;
        }
}
