package com.example.demo.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileProcessorService {

    public List<Double> procesarArchivo(MultipartFile archivo) {
        String fileName = archivo.getOriginalFilename();

        if (fileName == null) {
            throw new RuntimeException("Nombre de archivo inválido");
        }

        if (fileName.endsWith(".txt")) {
            return procesarTxt(archivo);
        } else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            return procesarExcel(archivo);
        } else {
            throw new RuntimeException("Formato de archivo no soportado: " + fileName);
        }
    }

    private List<Double> procesarTxt(MultipartFile archivo) {
        List<Double> datos = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(archivo.getInputStream()))) {

            String linea;
            boolean primeraLinea = true; // Saltar encabezado
            while ((linea = reader.readLine()) != null) {
                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }
                procesarLineaTxt(linea, datos);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error procesando archivo TXT", e);
        }

        return datos;
    }

    private void procesarLineaTxt(String linea, List<Double> datos) {
        linea = linea.trim();
        if (linea.isEmpty()) return;

        // Dividir por tabulador (asumiendo formato Nº\tPeso gr.)
        String[] partes = linea.split("\\t");
        if (partes.length >= 2) {
            try {
                String valorPeso = partes[1].trim();
                // Reemplazar coma por punto para parsear correctamente
                valorPeso = valorPeso.replace(",", "");
                datos.add(Double.parseDouble(valorPeso));
            } catch (NumberFormatException e) {
                // Ignorar valores no numéricos
            }
        }
    }

    private List<Double> procesarExcel(MultipartFile archivo) {
        List<Double> datos = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(archivo.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // Primera hoja

            boolean primeraFila = true;

            for (Row row : sheet) {
                if (primeraFila) {
                    primeraFila = false;
                    continue; // Saltar la fila de encabezados
                }

                // Leer solo la segunda columna (columna B, índice 1)
                Cell cell = row.getCell(2);
                if (cell != null) {
                    procesarCeldaExcel(cell, datos);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error procesando archivo Excel", e);
        }

        return datos;
    }

    private void procesarCeldaExcel(Cell cell, List<Double> datos) {
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    datos.add(cell.getNumericCellValue());
                    break;
                case STRING:
                    String valor = cell.getStringCellValue().trim();
                    if (!valor.isEmpty()) {
                        // Manejar números con comas como "1,103" -> 1103.0
                        valor = valor.replace(",", "");
                        datos.add(Double.parseDouble(valor));
                    }
                    break;
                case FORMULA:
                    // Para fórmulas, obtener el valor numérico resultante
                    if (cell.getCachedFormulaResultType() == CellType.NUMERIC) {
                        datos.add(cell.getNumericCellValue());
                    } else if (cell.getCachedFormulaResultType() == CellType.STRING) {
                        String valorFormula = cell.getStringCellValue().trim();
                        valorFormula = valorFormula.replace(",", "");
                        datos.add(Double.parseDouble(valorFormula));
                    }
                    break;
                default:
                    // Ignorar otros tipos de celdas
                    break;
            }
        } catch (NumberFormatException e) {
            // Ignorar celdas no numéricas
            System.out.println("Valor no numérico ignorado: " + cell.toString());
        }
    }
}