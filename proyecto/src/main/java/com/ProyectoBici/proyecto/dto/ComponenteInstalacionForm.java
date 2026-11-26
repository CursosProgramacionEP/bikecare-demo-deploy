package com.ProyectoBici.proyecto.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Adaptación del formulario de instalación (marca_{id}, modelo_{id},
 * fecha_{id}, km_{id}) a objetos ya validados. El servicio recibe esta lista,
 * nunca el Map crudo de la petición.
 */
public record ComponenteInstalacionForm(
        int idComponente,
        String marca,
        String modelo,
        LocalDate fechaInstalacion,
        BigDecimal kilometrajeInstalacion) {

    /**
     * Convierte los params del form a formularios válidos, deduplicando y
     * respetando el orden. Los ids nulos se ignoran.
     */
    public static List<ComponenteInstalacionForm> desdeParams(
            List<Integer> idsSeleccionados, Map<String, String> params, BigDecimal kmBici) {
        List<ComponenteInstalacionForm> formas = new ArrayList<>();
        if (idsSeleccionados == null || idsSeleccionados.isEmpty()) {
            return formas;
        }
        BigDecimal kmBase = kmBici != null ? kmBici : BigDecimal.ZERO;
        Set<Integer> vistos = new LinkedHashSet<>(idsSeleccionados);
        for (Integer id : vistos) {
            if (id == null) {
                continue;
            }
            formas.add(new ComponenteInstalacionForm(
                    id,
                    limpiar(params.get("marca_" + id), 80),
                    limpiar(params.get("modelo_" + id), 80),
                    leerFecha(params.get("fecha_" + id)),
                    leerKmInstalacion(params.get("km_" + id), kmBase)));
        }
        return formas;
    }

    // Texto opcional: vacío -> null, y se recorta al largo máximo de la columna.
    private static String limpiar(String texto, int max) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String limpio = texto.trim();
        return limpio.length() > max ? limpio.substring(0, max) : limpio;
    }

    // Si no ponen fecha (o es inválida/futura) se asume hoy.
    private static LocalDate leerFecha(String texto) {
        LocalDate hoy = LocalDate.now();
        if (texto == null || texto.isBlank()) {
            return hoy;
        }
        try {
            LocalDate fecha = LocalDate.parse(texto.trim());
            return fecha.isAfter(hoy) ? hoy : fecha;
        } catch (DateTimeParseException e) {
            return hoy;
        }
    }

    // Si no ponen km (o son inválidos) se asume que se instaló ahora (km actual de la bici).
    // Nunca puede ser mayor al km actual de la bici ni negativo.
    private static BigDecimal leerKmInstalacion(String texto, BigDecimal kmBici) {
        if (texto == null || texto.isBlank()) {
            return kmBici;
        }
        try {
            BigDecimal km = new BigDecimal(texto.trim()).setScale(2, RoundingMode.HALF_UP);
            if (km.signum() < 0) {
                return BigDecimal.ZERO;
            }
            return km.compareTo(kmBici) > 0 ? kmBici : km;
        } catch (NumberFormatException e) {
            return kmBici;
        }
    }
}
