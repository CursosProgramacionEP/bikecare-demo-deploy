package com.ProyectoBici.proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Estado de desgaste de un componente instalado, listo para mostrar en el card.
 * Todo se calcula por KILOMETRAJE: kmUsados / limiteKm.
 * idBc e idUmbral permiten al rol Taller editar el límite desde el Garaje.
 */
@Getter
@AllArgsConstructor
public class ComponenteEstado {
    private final String nombre;
    private final String icono;
    private final int kmUsados;
    private final int limiteKm;        // 0 = sin límite definido
    private final int porcentaje;      // puede pasar de 100
    private final int porcentajeBarra; // 0..100 para dibujar la barra
    private final String nivel;        // ok | alerta | critico
    private final String etiqueta;     // texto corto para mostrar
    // Id del registro bicicleta_componente (siempre presente).
    private final int idBc;
    // Id del umbral activo (puede ser null si el componente no tiene umbral).
    private final Integer idUmbral;
}
