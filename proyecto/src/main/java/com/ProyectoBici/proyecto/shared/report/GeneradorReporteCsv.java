package com.ProyectoBici.proyecto.shared.report;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

/** Genera CSV (UTF-8 con BOM para que Excel respete tildes). Sin dependencias extra. */
@Component
public class GeneradorReporteCsv implements GeneradorReporte {

    @Override
    public ReportFormat formato() {
        return ReportFormat.CSV;
    }

    @Override
    public GeneratedReport generar(ReportData datos) {
        StringBuilder contenido = new StringBuilder("\uFEFF");
        agregarFila(contenido, datos.getHeaders());
        for (List<Object> fila : datos.getRows()) {
            agregarFila(contenido, fila);
        }

        String nombre = slug(datos.getTitle()) + "_" + LocalDate.now() + "." + formato().getExtension();
        return new GeneratedReport(nombre, formato().getContentType(), contenido.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void agregarFila(StringBuilder contenido, List<?> celdas) {
        for (int i = 0; i < celdas.size(); i++) {
            if (i > 0) {
                contenido.append(',');
            }
            contenido.append(celda(celdas.get(i)));
        }
        contenido.append("\r\n");
    }

    private String celda(Object valor) {
        if (valor == null) {
            return "";
        }
        String texto = valor.toString();

        // Evita inyección de fórmulas en Excel (=, +, -, @) en textos escritos por usuarios
        if (!(valor instanceof Number) && !texto.isEmpty() && "=+-@\t\r".indexOf(texto.charAt(0)) >= 0) {
            texto = "'" + texto;
        }
        if (texto.contains(",") || texto.contains("\"") || texto.contains("\n") || texto.contains("\r")) {
            texto = "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }

    private String slug(String titulo) {
        String base = Normalizer.normalize(titulo == null ? "reporte" : titulo, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return base.isEmpty() ? "reporte" : base.toLowerCase();
    }
}
