package com.example.demo.controller;

import com.example.demo.model.AnalysisResult;
import com.example.demo.model.GraphicsResponse;
import com.example.demo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/estadistica")
@CrossOrigin(origins = "*")
public class StatisticsController {

    @Autowired
    private StatisticsService estadisticaService;

    @Autowired
    private FileProcessorService fileProcessorService;

    @PostMapping(value = "/analizar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalysisResult> analizarDatos(
            @RequestParam("archivo") MultipartFile archivo) {

        try {
            // Procesar archivo y obtener datos
            var datos = fileProcessorService.procesarArchivo(archivo);

            // Analizar datos
            AnalysisResult resultado = estadisticaService.analizarDatos(datos);

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            throw new RuntimeException("Error procesando archivo: " + e.getMessage(), e);
        }
    }

    @PostMapping(value = "/grafica/histograma", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> obtenerGraficaHistograma(
            @RequestParam("archivo") MultipartFile archivo) {

        try {
            var datos = fileProcessorService.procesarArchivo(archivo);
            AnalysisResult resultado = estadisticaService.analizarDatos(datos);
            byte[] graficaBytes = estadisticaService.generarGraficaHistograma(datos, resultado);

            return crearRespuestaImagen(graficaBytes, "histograma.png");

        } catch (Exception e) {
            throw new RuntimeException("Error generando gráfica de histograma: " + e.getMessage(), e);
        }
    }

    @PostMapping(value = "/grafica/barras", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> obtenerGraficaBarras(
            @RequestParam("archivo") MultipartFile archivo) {

        try {
            var datos = fileProcessorService.procesarArchivo(archivo);
            AnalysisResult resultado = estadisticaService.analizarDatos(datos);
            byte[] graficaBytes = estadisticaService.generarGraficaBarras(datos, resultado);

            return crearRespuestaImagen(graficaBytes, "barras_chebyshev.png");

        } catch (Exception e) {
            throw new RuntimeException("Error generando gráfica de barras: " + e.getMessage(), e);
        }
    }

    @PostMapping(value = "/graficas", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GraphicsResponse> obtenerTodasLasGraficas(
            @RequestParam("archivo") MultipartFile archivo) {

        try {
            var datos = fileProcessorService.procesarArchivo(archivo);
            AnalysisResult resultado = estadisticaService.analizarDatos(datos);

            byte[] histogramaBytes = estadisticaService.generarGraficaHistograma(datos, resultado);
            byte[] barrasBytes = estadisticaService.generarGraficaBarras(datos, resultado);

            GraphicsResponse response = new GraphicsResponse();
            response.setHistograma(histogramaBytes);
            response.setBarras(barrasBytes);
            response.setResultado(resultado);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new RuntimeException("Error generando gráficas: " + e.getMessage(), e);
        }
    }

    private ResponseEntity<byte[]> crearRespuestaImagen(byte[] imagenBytes, String nombreArchivo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("attachment", nombreArchivo);

        return ResponseEntity.ok()
                .headers(headers)
                .body(imagenBytes);
    }
}