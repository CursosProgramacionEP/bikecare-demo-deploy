package com.ProyectoBici.proyecto.dto;

import java.math.BigDecimal;

import com.ProyectoBici.proyecto.entity.Bicicleta;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Contexto mínimo de la bici que viaja a Gemini.
 * Solo datos útiles para el diagnóstico (desgaste/uso).
 * Excluye a propósito: id, usuario, número de serie, notas, foto y fechas.
 */
@Getter
@AllArgsConstructor
public class DiagnosticoBiciContexto {

    private final String marca;
    private final String modelo;
    private final Integer anio;
    private final String tipo;
    private final BigDecimal kilometrajeTotal;
    private final String terrenoHabitual;
    private final String transmision;

    public static DiagnosticoBiciContexto desde(Bicicleta b) {
        return new DiagnosticoBiciContexto(
                b.getMarca(), b.getModelo(), b.getAnio(), b.getTipo(),
                b.getKilometrajeTotal(), b.getTerrenoHabitual(), b.getTransmision());
    }

    /** Líneas "clave: valor" omitiendo nulos, listas para el prompt. */
    public String toPrompt() {
        StringBuilder sb = new StringBuilder();
        agregar(sb, "Marca/modelo", marcaModelo());
        agregar(sb, "Año", anio == null ? null : String.valueOf(anio));
        agregar(sb, "Tipo", tipo);
        agregar(sb, "Odómetro", kilometrajeTotal == null ? null : kilometrajeTotal.toPlainString() + " km");
        agregar(sb, "Terreno habitual", terrenoHabitual);
        agregar(sb, "Transmisión", transmision);
        return sb.length() == 0 ? "no especificada" : sb.toString().trim();
    }

    private String marcaModelo() {
        String m = marca == null ? "" : marca.trim();
        String mo = modelo == null ? "" : modelo.trim();
        return (m + " " + mo).trim().isEmpty() ? null : (m + " " + mo).trim();
    }

    private static void agregar(StringBuilder sb, String clave, String valor) {
        if (valor != null && !valor.isBlank()) {
            sb.append("- ").append(clave).append(": ").append(valor.trim()).append("\n");
        }
    }
}
