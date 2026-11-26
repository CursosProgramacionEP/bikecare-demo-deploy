package com.ProyectoBici.proyecto.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.ProyectoBici.proyecto.dto.DiagnosticoResultado;
import com.ProyectoBici.proyecto.dto.DiagnosticoResultado.Paso;

/**
 * REGLA DE NEGOCIO del diagnóstico preliminar (mock sin IA real).
 * Clasifica por palabras clave de los síntomas + componentes marcados.
 * No sabe nada de HTTP/sesión; el controlador adapta la petición.
 */

//HU10: Analiza sintomas con IA
@Service
public class DiagnosticoIAService {

    public DiagnosticoResultado diagnosticar(String descripcionRaw, List<String> componentes) {
        String texto = descripcionRaw == null ? "" : descripcionRaw.toLowerCase(Locale.ROOT);
        List<String> comps = componentes == null ? List.of() : componentes;

        // Caso crítico: posible daño estructural -> ALTO.
        if (contiene(texto, "fisura", "roto", "partid", "quebrad", "tronido fuerte", "no frena")) {
            return new DiagnosticoResultado(
                    "ALTO", "Alta",
                    "⛔ Evita usar la bicicleta hasta revisarla en taller. Podría haber un daño estructural o de frenado.",
                    "Posible daño estructural o falla crítica de frenado",
                    "Los síntomas describen una falla que compromete tu seguridad (fisura, rotura o pérdida de frenado). No es un desgaste normal.",
                    List.of(
                            new Paso("Deja de rodar", "No uses la bici hasta que un mecánico la revise."),
                            new Paso("Revisión en taller", "Pide inspección de cuadro, horquilla y sistema de frenos."),
                            new Paso("No intentes repararlo solo", "Un apriete o pegado casero puede empeorar el daño.")));
        }

        // Frenos: chirrido / esponjoso -> MEDIO.
        if (contiene(texto, "freno", "chirr", "esponjoso", "pastilla", "disco")
                || comps.contains("Frenos")) {
            return new DiagnosticoResultado(
                    "MEDIO", "Media",
                    "⚠️ Puedes usar la bici con precaución en trayectos cortos, pero agenda el servicio esta semana.",
                    "Pastillas contaminadas o desajuste de frenos",
                    "El chirrido al frenar y la sensación esponjosa suelen deberse a pastillas contaminadas o aire en el sistema / cable destensado.",
                    List.of(
                            new Paso("Limpieza de discos", "Limpia los discos con alcohol isopropílico y lija suave las pastillas."),
                            new Paso("Ajuste de frenos", "Centra el cáliper y ajusta la tensión del cable o purga si es hidráulico."),
                            new Paso("Prueba controlada", "Frena progresivamente en plano; si sigue sonando, cambia pastillas.")));
        }

        // Transmisión: salto de cadena / crujido -> MEDIO.
        if (contiene(texto, "salta", "cadena", "piñon", "pinon", "cambio")
                || (comps.contains("Cadena") && contiene(texto, "salta", "cruj"))) {
            return new DiagnosticoResultado(
                    "MEDIO", "Media",
                    "⚠️ Evita rutas largas o subidas fuertes hasta el servicio para no desgastar el cassette.",
                    "Cadena estirada o desviador desajustado",
                    "El salto en un piñón concreto bajo carga indica estiramiento de cadena (>0.75%) o tensión incorrecta del cable de cambios.",
                    List.of(
                            new Paso("Medir la cadena", "Usa un calibrador; si supera 0.75%, toca reemplazarla."),
                            new Paso("Limpieza y lubricación", "Desengrasa, seca y lubrica gota a gota."),
                            new Paso("Ajuste de cambios", "Regula tensión del cable y topes H/L en taller si no mejora.")));
        }

        // Caso por defecto (el de la maqueta): suciedad / rodamientos -> BAJO.
        return new DiagnosticoResultado(
                "BAJO", "Bajo",
                "✅ Puedes seguir usando tu bicicleta con normalidad, pero atiende el mantenimiento pronto para evitar un desgaste mayor.",
                "Falta de lubricación o suciedad en rodamientos",
                "El crujido metálico bajo carga, tras rodar con barro y varias semanas sin lubricación, coincide con fricción por suciedad acumulada en cadena y rodamientos. No se detectan signos de fractura o daño estructural.",
                List.of(
                        new Paso("Limpieza básica", "Lava la transmisión con desengrasante suave, enjuaga y seca bien para retirar el barro acumulado."),
                        new Paso("Lubricación de cadena y rodamientos", "Aplica lubricante específico gota a gota, retira el exceso y gira las bielas para distribuirlo."),
                        new Paso("Prueba de rodaje corto", "Haz una salida de 10 min; si el ruido persiste, agenda revisión de rodamientos en taller.")));
    }

    private boolean contiene(String texto, String... claves) {
        for (String c : claves) {
            if (texto.contains(c)) {
                return true;
            }
        }
        return false;
    }
}
