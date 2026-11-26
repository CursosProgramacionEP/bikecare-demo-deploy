package com.ProyectoBici.proyecto.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ProyectoBici.proyecto.entity.Rol;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.repository.RolRepository;
import com.ProyectoBici.proyecto.repository.UsuarioRepository;

@Service
public class UsuarioService {

    public static final String ROL_ADMIN = "ADMIN";
    private static final String ROL_CICLISTA = "ciclista";

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario autenticar(String correo, String contrasenia) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);
        if (usuarioOpt.isEmpty()) {
            return null;
        }

        Usuario usuario = usuarioOpt.get();
        String almacenada = usuario.getContrasenia();
        if (almacenada == null || contrasenia == null) {
            return null;
        }

        if (esHashBcrypt(almacenada)) {
            return passwordEncoder.matches(contrasenia, almacenada) ? usuario : null;
        }

        // Compatibilidad con registros antiguos en texto plano: al iniciar sesion
        // correctamente se reemplaza la clave por su hash BCrypt.
        if (almacenada.equals(contrasenia)) {
            usuario.setContrasenia(passwordEncoder.encode(contrasenia));
            usuarioRepository.save(usuario);
            return usuario;
        }
        return null;
    }

    private static boolean esHashBcrypt(String valor) {
        return valor.startsWith("$2a$")
                || valor.startsWith("$2b$")
                || valor.startsWith("$2y$");
    }

    public boolean existeCorreo(String correo) {
        return correo != null && usuarioRepository.existsByCorreoIgnoreCase(correo.trim());
    }

    public void guardarUsuario(
            String nombre,
            String apellidos,
            String correo,
            String contrasenia) {

        Usuario usuario = new Usuario();

        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setCorreo(correo);
        usuario.setContrasenia(passwordEncoder.encode(contrasenia));

        Rol rolCiclista = rolRepository.findByNombreRol(ROL_CICLISTA)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        usuario.setRol(rolCiclista);

        usuarioRepository.save(usuario);
    }

    public static boolean esTaller(Usuario usuario) {
        return usuario != null
                && usuario.getRol() != null
                && "taller".equalsIgnoreCase(usuario.getRol().getNombreRol());
    }

    /**
     * El administrador se identifica por su rol; el alta se hace por SQL
     * (docs/sql/crear-admin.sql).
     */
    public static boolean esAdminUnico(Usuario usuario) {
        return usuario != null
                && usuario.getRol() != null
                && ROL_ADMIN.equalsIgnoreCase(usuario.getRol().getNombreRol());
    }

    @Transactional(readOnly = true)
    public boolean esPremium(int idUsuario) {
        return usuarioRepository.findById(idUsuario).map(Usuario::isEsPremium).orElse(false);
    }

    public static final int MAX_INTENTOS = 5;
    public static final int HORAS_BLOQUEO = 24;

    public boolean estaBloqueado(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .map(u -> u.getBloqueadoHasta() != null
                        && u.getBloqueadoHasta().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    public void registrarFallo(String correo) {
        usuarioRepository.findByCorreo(correo).ifPresent(u -> {
            u.setIntentosFallidos(u.getIntentosFallidos() + 1);
            if (u.getIntentosFallidos() >= MAX_INTENTOS) {
                u.setBloqueadoHasta(LocalDateTime.now().plusHours(HORAS_BLOQUEO));
                u.setIntentosFallidos(0);
            }
            usuarioRepository.save(u);
        });
    }

    public void limpiarIntentos(Usuario u) {
        if (u.getIntentosFallidos() != 0 || u.getBloqueadoHasta() != null) {
            u.setIntentosFallidos(0);
            u.setBloqueadoHasta(null);
            usuarioRepository.save(u);
        }
    }
}