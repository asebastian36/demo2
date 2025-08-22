package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.util.StatisticsUtil;
import org.jfree.chart.*;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.*;
import org.springframework.stereotype.Service;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class StatisticsService {

    public AnalysisResult analizarDatos(List<Double> datos) {
        double media = StatisticsUtil.calcularMedia(datos);
        double desviacion = StatisticsUtil.calcularDesviacionEstandar(datos, media);

        AnalysisResult resultado = new AnalysisResult();
        resultado.setMedia(media);
        resultado.setDesviacionEstandar(desviacion);
        resultado.setTotalDatos(datos.size());
        resultado.setResultadosChebyshev(calcularChebyshev(datos, media, desviacion));

        return resultado;
    }

    public byte[] generarGraficaHistograma(List<Double> datos, AnalysisResult resultado) {
        try {
            // Normalizar datos
            List<Double> datosNormalizados = StatisticsUtil.normalizarDatos(
                    datos, resultado.getMedia(), resultado.getDesviacionEstandar());

            // Crear gráfica de histograma
            JFreeChart chart = crearChartHistograma(datosNormalizados, resultado);

            // Convertir a bytes
            return convertirChartABytes(chart);

        } catch (Exception e) {
            throw new RuntimeException("Error generando gráfica de histograma", e);
        }
    }

    public byte[] generarGraficaBarras(List<Double> datos, AnalysisResult resultado) {
        try {
            // Crear gráfica de barras
            JFreeChart chart = crearChartBarras(datos, resultado);

            // Convertir a bytes
            return convertirChartABytes(chart);

        } catch (Exception e) {
            throw new RuntimeException("Error generando gráfica de barras", e);
        }
    }

    private byte[] convertirChartABytes(JFreeChart chart) throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(stream, chart, 800, 600);
        return stream.toByteArray();
    }

    private List<ResultChebyshev> calcularChebyshev(List<Double> datos,
                                                    double media, double desviacion) {
        return List.of(1, 2, 3).stream()
                .map(k -> calcularResultadoK(datos, media, desviacion, k))
                .toList();
    }

    private ResultChebyshev calcularResultadoK(List<Double> datos,
                                               double media, double desviacion, int k) {
        ResultChebyshev resultado = new ResultChebyshev();
        resultado.setK(k);

        // Calcular intervalo
        double limiteInferior = media - k * desviacion;
        double limiteSuperior = media + k * desviacion;
        resultado.setLimiteInferior(limiteInferior);
        resultado.setLimiteSuperior(limiteSuperior);

        // Calcular mínimo teórico
        resultado.setMinimoTeorico((1 - 1.0 / (k * k)) * 100);

        // Contar datos en intervalo usando el método optimizado
        int datosEnIntervalo = StatisticsUtil.contarDatosEnIntervalo(datos, limiteInferior, limiteSuperior);
        resultado.setDatosEnIntervalo(datosEnIntervalo);

        // Calcular porcentaje real
        resultado.setPorcentajeReal((double) datosEnIntervalo / datos.size() * 100);

        // Verificar si cumple criterio
        resultado.setCumpleCriterio(resultado.getPorcentajeReal() >= resultado.getMinimoTeorico());

        return resultado;
    }

    private JFreeChart crearChartHistograma(List<Double> datosNormalizados, AnalysisResult resultado) {
        // Crear dataset para histograma
        XYSeries histogramSeries = new XYSeries("Histograma");

        // Calcular histograma con límites inteligentes
        int bins = 16;
        double[] limites = StatisticsUtil.calcularLimitesHistograma(datosNormalizados);
        double min = limites[0];
        double max = limites[1];
        double binWidth = (max - min) / bins;

        int[] frequencies = new int[bins];
        for (double dato : datosNormalizados) {
            if (dato >= min && dato <= max) {
                int binIndex = (int) ((dato - min) / binWidth);
                if (binIndex >= bins) binIndex = bins - 1;
                if (binIndex < 0) binIndex = 0;
                frequencies[binIndex]++;
            }
        }

        // Agregar puntos al histograma
        for (int i = 0; i < bins; i++) {
            double x = min + i * binWidth + binWidth / 2;
            histogramSeries.add(x, frequencies[i]);
        }

        // Crear curva KDE
        XYSeries kdeSeries = calcularCurvaKDE(datosNormalizados);

        // Crear dataset
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(histogramSeries);
        dataset.addSeries(kdeSeries);

        // Crear chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Distribución de Datos - Desviaciones Estándar",
                "Desviaciones estándar desde la media (σ)",
                "Frecuencia / Densidad",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Personalizar chart
        personalizarChartHistograma(chart);

        return chart;
    }

    private JFreeChart crearChartBarras(List<Double> datos, AnalysisResult resultado) {
        // Normalizar datos para consistencia con el histograma
        List<Double> datosNormalizados = StatisticsUtil.normalizarDatos(
                datos, resultado.getMedia(), resultado.getDesviacionEstandar());

        // Crear dataset para gráfica de barras del histograma
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Calcular histograma con los mismos parámetros que el gráfico principal
        int bins = 16;
        double[] limites = StatisticsUtil.calcularLimitesHistograma(datosNormalizados);
        double min = limites[0];
        double max = limites[1];
        double binWidth = (max - min) / bins;

        int[] frequencies = new int[bins];
        for (double dato : datosNormalizados) {
            if (dato >= min && dato <= max) {
                int binIndex = (int) ((dato - min) / binWidth);
                if (binIndex >= bins) binIndex = bins - 1;
                if (binIndex < 0) binIndex = 0;
                frequencies[binIndex]++;
            }
        }

        // Agregar datos al dataset
        for (int i = 0; i < bins; i++) {
            double inicioIntervalo = min + i * binWidth;
            double finIntervalo = min + (i + 1) * binWidth;
            String intervalo = String.format("[%.2f, %.2f]", inicioIntervalo, finIntervalo);
            dataset.addValue(frequencies[i], "Frecuencia", intervalo);
        }

        // Crear chart de barras
        JFreeChart chart = ChartFactory.createBarChart(
                "Distribución de Frecuencias - Histograma de Barras",
                "Intervalos (desviaciones estándar desde la media)",
                "Frecuencia",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Personalizar chart
        personalizarChartBarras(chart);

        return chart;
    }
    private XYSeries calcularCurvaKDE(List<Double> datos) {
        XYSeries series = new XYSeries("Curva KDE");

        // Calcular media y desviación de los datos NORMALIZADOS
        double mediaNormalizados = StatisticsUtil.calcularMedia(datos);
        double desviacionNormalizados = StatisticsUtil.calcularDesviacionEstandar(datos, mediaNormalizados);

        // Parámetros para KDE
        double bandwidth = calcularBandwidth(datos, desviacionNormalizados);

        // Calcular puntos de la curva
        double minX = -4.0;
        double maxX = 4.0;
        for (double x = minX; x <= maxX; x += 0.1) {
            double densidad = calcularDensidadKDE(datos, x, bandwidth);
            series.add(x, densidad * 100); // Escalar para visualización
        }

        return series;
    }

    private double calcularDensidadKDE(List<Double> datos, double x, double bandwidth) {
        if (bandwidth <= 0) return 0;

        double densidad = 0;
        for (double dato : datos) {
            densidad += kernelGaussiano((x - dato) / bandwidth);
        }
        return densidad / (datos.size() * bandwidth);
    }

    private double kernelGaussiano(double u) {
        return (1.0 / Math.sqrt(2 * Math.PI)) * Math.exp(-0.5 * u * u);
    }

    private double calcularBandwidth(List<Double> datos, double desviacion) {
        if (datos.size() <= 1) return 1.0;

        // Regla de Silverman para datos normalizados
        return 1.06 * desviacion * Math.pow(datos.size(), -0.2);
    }

    private void personalizarChartHistograma(JFreeChart chart) {
        XYPlot plot = chart.getXYPlot();

        // Configurar renderer
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(65, 105, 225, 150)); // Azul transparente para histograma
        renderer.setSeriesPaint(1, new Color(220, 20, 60));  // Rojo para curva KDE
        renderer.setSeriesStroke(1, new BasicStroke(2.5f));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShapesVisible(1, false);
        renderer.setSeriesShape(0, new java.awt.Rectangle(5, 5)); // Forma para histograma

        plot.setRenderer(renderer);

        // Líneas verticales para desviaciones estándar
        plot.addDomainMarker(new org.jfree.chart.plot.ValueMarker(0,
                new Color(0, 100, 0), new BasicStroke(2.0f))); // Media

        for (int k = 1; k <= 3; k++) {
            plot.addDomainMarker(new org.jfree.chart.plot.ValueMarker(k,
                    new Color(255, 165, 0), new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL, 1.0f, new float[]{5.0f, 5.0f}, 0.0f)));
            plot.addDomainMarker(new org.jfree.chart.plot.ValueMarker(-k,
                    new Color(255, 165, 0), new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL, 1.0f, new float[]{5.0f, 5.0f}, 0.0f)));
        }

        // Configurar fondo y grid
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(192, 192, 192, 100));
        plot.setDomainGridlinePaint(new Color(192, 192, 192, 100));

        // Mejorar la visualización del histograma
        plot.getRangeAxis().setLowerBound(0); // Empezar desde 0 en el eje Y
    }

    private void personalizarChartBarras(JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();

        // Configurar colores
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(65, 105, 225)); // Azul para las barras
        renderer.setShadowVisible(false);

        // Configurar fondo
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(192, 192, 192, 100));

        // Rotar etiquetas del eje X para mejor visualización
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        // Mejorar visualización
        plot.getRangeAxis().setLowerBound(0);
    }
}