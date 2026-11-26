package com.ProyectoBici.proyecto.dto;


import java.util.List;

import com.ProyectoBici.proyecto.entity.Bicicleta;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Una bici junto con el estado de sus componentes (lo que pinta cada card del garaje). */
@Getter
@AllArgsConstructor
public class BicicletaCard {
    private final Bicicleta bicicleta;
    private final List<ComponenteEstado> componentes;
}
