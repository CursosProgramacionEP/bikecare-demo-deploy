package com.ProyectoBici.proyecto.dto;


import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

/**
 * Datos que llegan del formulario "Añadir bicicleta".
 * Los nombres coinciden con los name="" de los inputs del HTML.
 */
@Data
public class BicicletaForm {
    private String marca;
    private String modelo;
    private String tipo;
    private Integer anio;
    private String numeroSerie;
    private String terrenoHabitual;
    private BigDecimal kilometrajeTotal;
    private BigDecimal peso;
    private String transmision;
    private String tamanoCuadro;
    private String notas;
    private MultipartFile foto;
}
