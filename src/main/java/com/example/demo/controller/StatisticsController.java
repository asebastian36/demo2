package com.example.demo.controller;

import com.example.demo.model.AnalysisResult;
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

    @PostMapping(value = "/grafica", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> obtenerGrafica(
            @RequestParam("archivo") MultipartFile archivo) {

        try {
            var datos = fileProcessorService.procesarArchivo(archivo);
            AnalysisResult resultado = estadisticaService.analizarDatos(datos);
            byte[] graficaBytes = estadisticaService.generarGrafica(datos, resultado);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", "grafica.png");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(graficaBytes);

        } catch (Exception e) {
            throw new RuntimeException("Error generando gráfica: " + e.getMessage(), e);
        }
    }
}
