DROP DATABASE IF EXISTS bikecare_db;
CREATE DATABASE bikecare_db;
USE bikecare_db;

-- 1. TABLA ROL
CREATE TABLE rol (
  id_rol INT AUTO_INCREMENT PRIMARY KEY,
  nombre_rol VARCHAR(50) NOT NULL
);

-- 2. TABLA COMPONENTE
CREATE TABLE componente (
  id_componente INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  categoria VARCHAR(80) NOT NULL,
  descripcion VARCHAR(255) NULL,
  vida_util_referencial DECIMAL(10,2) NULL
);

-- 3. TABLA USUARIO
CREATE TABLE usuario (
  id_usuario INT AUTO_INCREMENT PRIMARY KEY,
  id_rol INT NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  apellidos VARCHAR(100) NOT NULL,
  correo VARCHAR(150) NOT NULL UNIQUE,
  contrasenia VARCHAR(255) NOT NULL,
  FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
);

-- 4. TABLA BICICLETA
CREATE TABLE bicicleta (
  id_bicicleta INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario INT NOT NULL,
  marca VARCHAR(80) NOT NULL,
  modelo VARCHAR(80) NOT NULL,
  tipo VARCHAR(50) NULL,
  anio INT NULL,
  numero_serie VARCHAR(100) NULL,
  kilometraje_total DECIMAL(10,2) DEFAULT 0.00,
  terreno_habitual VARCHAR(100) NULL,
  fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
  estado VARCHAR(20) DEFAULT 'activa',
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
);

-- 5. TABLA RUTA
CREATE TABLE ruta (
  id_ruta INT AUTO_INCREMENT PRIMARY KEY,
  id_bicicleta INT NOT NULL,
  distancia_km DECIMAL(8,2) NOT NULL,
  fecha_recorrido DATETIME DEFAULT CURRENT_TIMESTAMP,
  origen_registro VARCHAR(50) NULL,
  FOREIGN KEY (id_bicicleta) REFERENCES bicicleta(id_bicicleta) ON DELETE CASCADE
);

-- 6. TABLA BICICLETA_COMPONENTE
CREATE TABLE bicicleta_componente (
  id_bicicleta_componente INT AUTO_INCREMENT PRIMARY KEY,
  id_bicicleta INT NOT NULL,
  id_componente INT NOT NULL,
  marca VARCHAR(80) NULL,
  modelo VARCHAR(80) NULL,
  numero_serie VARCHAR(100) NULL,
  fecha_instalacion DATE NOT NULL,
  kilometraje_instalacion DECIMAL(10,2) DEFAULT 0.00,
  kilometraje_actual DECIMAL(10,2) DEFAULT 0.00,
  estado VARCHAR(20) DEFAULT 'activo',
  FOREIGN KEY (id_bicicleta) REFERENCES bicicleta(id_bicicleta) ON DELETE CASCADE,
  FOREIGN KEY (id_componente) REFERENCES componente(id_componente)
);

-- 7. TABLA UMBRAL
CREATE TABLE umbral (
  id_umbral INT AUTO_INCREMENT PRIMARY KEY,
  id_bicicleta_componente INT NOT NULL,
  limite_km DECIMAL(10,2) NOT NULL,
  porcentaje_alerta INT NOT NULL,
  activo INT DEFAULT 1,
  FOREIGN KEY (id_bicicleta_componente) REFERENCES bicicleta_componente(id_bicicleta_componente) ON DELETE CASCADE
);

-- 8. TABLA ALERTA
CREATE TABLE alerta (
  id_alerta INT AUTO_INCREMENT PRIMARY KEY,
  id_bicicleta_componente INT NOT NULL,
  id_umbral INT NULL,
  tipo_alerta VARCHAR(50) NOT NULL,
  mensaje VARCHAR(255) NOT NULL,
  kilometraje_actual DECIMAL(10,2) NOT NULL,
  fecha_generacion DATETIME DEFAULT CURRENT_TIMESTAMP,
  atendida INT DEFAULT 0,
  FOREIGN KEY (id_bicicleta_componente) REFERENCES bicicleta_componente(id_bicicleta_componente) ON DELETE CASCADE,
  FOREIGN KEY (id_umbral) REFERENCES umbral(id_umbral) ON DELETE SET NULL
);
-- 9. TABLA MANTENIMIENTO
CREATE TABLE mantenimiento (
  id_mantenimiento INT AUTO_INCREMENT PRIMARY KEY,
  id_bicicleta INT NOT NULL,
  id_componente INT NULL,
  tipo_servicio VARCHAR(80) NOT NULL,
  descripcion TEXT NULL,
  costo DECIMAL(8,2) DEFAULT 0.00,
  kilometraje_servicio DECIMAL(10,2) NOT NULL,
  fecha_servicio DATE NOT NULL,
  taller_mecanico VARCHAR(100) NULL,
  FOREIGN KEY (id_bicicleta) REFERENCES bicicleta(id_bicicleta) ON DELETE CASCADE,
  FOREIGN KEY (id_componente) REFERENCES componente(id_componente) ON DELETE SET NULL
);