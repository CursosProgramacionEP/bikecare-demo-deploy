package com.ProyectoBici.proyecto.shared.report;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;

/**
 * Datos neutros de un reporte (título, columnas y filas), independientes del formato final.
 *
 * <pre>
 * ReportData datos = ReportData.of("Estado del garaje", "Bicicleta", "Km")
 *         .addRow("Trek Marlin", 1200)
 *         .addRow("Giant Talon", 800);
 * </pre>
 */
@Getter
public class ReportData {

    private final String title;
    private final List<String> headers;
    private final List<List<Object>> rows = new ArrayList<>();
    private final LocalDateTime generatedAt = LocalDateTime.now();

    private ReportData(String title, List<String> headers) {
        this.title = title;
        this.headers = headers;
    }

    public static ReportData of(String title, String... headers) {
        return new ReportData(title, Arrays.asList(headers));
    }

    public ReportData addRow(Object... cells) {
        rows.add(Arrays.asList(cells));
        return this;
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }
}
