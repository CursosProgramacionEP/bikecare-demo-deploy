package com.ProyectoBici.proyecto.dto;

import java.math.BigDecimal;
import java.util.List;

import com.ProyectoBici.proyecto.entity.Bicicleta;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Solo DATOS del panel (lo que pinta modulo-panelControl.html).
 * Lo arma PanelService; el controlador solo lo publica al Model.
 */
@Getter
@AllArgsConstructor
public class PanelResumen {
    private final List<Bicicleta> bicicletas;          // opciones del <select>
    private final BigDecimal kmTotales;                // suma de odómetros
    private final int totalAlertas;                    // alerta/crítico en todas las bicis
    private final Bicicleta biciSeleccionada;          // detalle inferior (null si vacío)
    private final List<ComponenteEstado> componentesSel;
    private final List<ComponenteEstado> alertasSel;

    // Derivado: no se guarda, se calcula (tarjeta 1).
    public int getTotalBicis() {
        return bicicletas == null ? 0 : bicicletas.size();
    }
}
