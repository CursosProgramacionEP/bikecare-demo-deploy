-- ============================================================================
-- Datos iniciales para PostgreSQL (Neon)
--
-- Las tablas las crea Hibernate al iniciar la aplicación (ddl-auto=update).
-- Este script se ejecuta DESPUÉS del primer despliegue, en el SQL Editor de Neon.
-- Sin el rol 'ciclista', el registro de usuarios falla con "Rol no encontrado".
-- ============================================================================

-- Roles que usa la aplicación (UsuarioService)
INSERT INTO rol (nombre_rol)
SELECT v.nombre FROM (VALUES ('ciclista'), ('taller'), ('ADMIN')) AS v(nombre)
WHERE NOT EXISTS (SELECT 1 FROM rol r WHERE r.nombre_rol = v.nombre);

-- Catálogo básico de componentes (datos de ejemplo)
INSERT INTO componente (nombre, categoria, descripcion, vida_util_referencial)
SELECT v.* FROM (VALUES
  ('Cadena',             'Transmisión', 'Cadena de transmisión',         3000.00),
  ('Pastillas de freno', 'Frenos',      'Pastillas para freno de disco', 2000.00),
  ('Llanta delantera',   'Ruedas',      'Neumático delantero',           4000.00),
  ('Llanta trasera',     'Ruedas',      'Neumático trasero',             3000.00),
  ('Cassette',           'Transmisión', 'Piñones traseros',              8000.00)
) AS v(nombre, categoria, descripcion, vida_util_referencial)
WHERE NOT EXISTS (SELECT 1 FROM componente c WHERE c.nombre = v.nombre);
