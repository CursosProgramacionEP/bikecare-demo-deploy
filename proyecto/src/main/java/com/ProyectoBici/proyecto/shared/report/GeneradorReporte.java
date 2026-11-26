package com.ProyectoBici.proyecto.shared.report;

/** Estrategia: sabe generar reportes en UN formato. Cada implementación @Component se registra sola. */
public interface GeneradorReporte {

    ReportFormat formato();

    GeneratedReport generar(ReportData datos);
}
