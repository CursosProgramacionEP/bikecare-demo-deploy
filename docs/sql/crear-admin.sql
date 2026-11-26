-- ============================================================================
-- Alta del usuario administrador
--
-- El administrador ya NO se crea desde el codigo. Es un dato de la base:
-- se registra una sola vez con este script y su clave se cambia aqui mismo
-- cuando haga falta.
--
-- Requisitos previos:
--   * El rol 'ADMIN' debe existir en la tabla rol.
--   * La columna usuario.contrasenia acepta la clave en TEXTO PLANO o su hash
--     BCrypt ($2a$...). Al primer login exitoso, UsuarioService.autenticar
--     reemplaza el texto plano por su hash, asi que los UPDATE de abajo
--     pueden seguir usando la clave tal cual.
-- ============================================================================

-- 1) Asegurar el rol ADMIN (no duplica si ya existe)
INSERT INTO rol (nombre_rol)
SELECT 'ADMIN'  -- PostgreSQL no usa FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre_rol = 'ADMIN');

-- 2) Crear el administrador (no duplica si el correo ya existe)
INSERT INTO usuario (id_rol, nombre, apellidos, correo, contrasenia, es_premium)
SELECT r.id_rol, 'Admin', 'BikeCare', 'CAMBIAR@dominio.com', 'CAMBIAR_CLAVE', false
FROM rol r
WHERE r.nombre_rol = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.correo = 'CAMBIAR@dominio.com');

-- ============================================================================
-- Si el administrador ya existe y solo quieres cambiar sus datos:
--
-- UPDATE usuario
-- SET correo      = 'CAMBIAR@dominio.com',
--     contrasenia = 'CAMBIAR_CLAVE'
-- WHERE correo = 'valor-anterior@dominio.com';
--
-- Comprobar que quedo como admin:
--
-- SELECT u.id_usuario, u.correo, r.nombre_rol
-- FROM usuario u JOIN rol r ON r.id_rol = u.id_rol
-- WHERE r.nombre_rol = 'ADMIN';
-- ============================================================================
