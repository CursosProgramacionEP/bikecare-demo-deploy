package com.ProyectoBici.proyecto.shared.report;

import java.util.Set;

/** Punto de entrada para generar reportes desde cualquier controlador o servicio. */
public interface ReportService {

    /**
     * @throws com.ProyectoBici.proyecto.shared.exception.BusinessException si no hay datos
     *                                                                      o el formato no está disponible
     */
    GeneratedReport generate(ReportFormat formato, ReportData data);

    Set<ReportFormat> supportedFormats();
}
