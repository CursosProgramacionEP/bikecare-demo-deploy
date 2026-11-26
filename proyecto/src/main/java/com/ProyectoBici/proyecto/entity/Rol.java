package com.ProyectoBici.proyecto.entity;

import jakarta.persistence.Column;

/**
 * Es una entidad que asigna el rango que tiene el usuario dentro de la aplicacion
 */

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "rol")
public class Rol {
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     
     @Column(name = "id_rol")
     private int idRol;

     @Column(name = "nombre_rol")
     private String nombreRol;
}
