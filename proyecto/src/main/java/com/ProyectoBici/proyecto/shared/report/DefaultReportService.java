package com.ProyectoBici.proyecto.shared.report;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.ProyectoBici.proyecto.shared.exception.BusinessException;
import com.ProyectoBici.proyecto.shared.message.MessageType;

/** Delega en el GeneradorReporte registrado para cada formato. */
@Service
public class DefaultReportService implements ReportService {

    private final Map<ReportFormat, GeneradorReporte> generadores = new EnumMap<>(ReportFormat.class);

    // Spring inyecta todos los GeneradorReporte que existan
    public DefaultReportService(List<GeneradorReporte> listaGeneradores) {
        listaGeneradores.forEach(g -> generadores.put(g.formato(), g));
    }

    @Override
    public GeneratedReport generate(ReportFormat format, ReportData data) {
        GeneradorReporte generador = generadores.get(format);
        if (generador == null) {
            throw new BusinessException("El formato de reporte " + format + " no está disponible.");
        }
        if (data.isEmpty()) {
            throw new BusinessException(MessageType.WARNING, "No hay datos para generar el reporte.");
        }
        return generador.generar(data);
    }

    @Override
    public Set<ReportFormat> supportedFormats() {
        return generadores.keySet();
    }
}
