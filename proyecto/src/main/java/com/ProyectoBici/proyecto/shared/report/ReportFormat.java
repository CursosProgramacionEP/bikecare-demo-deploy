package com.ProyectoBici.proyecto.shared.report;

/** Formatos de reporte. Para añadir uno (ej. PDF) agrega el valor y un GeneradorReporte nuevo. */
public enum ReportFormat {

    CSV("text/csv; charset=UTF-8", "csv");

    private final String contentType;
    private final String extension;

    ReportFormat(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String getContentType() {
        return contentType;
    }

    public String getExtension() {
        return extension;
    }
}
