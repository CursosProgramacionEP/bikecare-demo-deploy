package com.ProyectoBici.proyecto.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.service.BicicletaService;
import com.ProyectoBici.proyecto.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

/**
 * Solo el LISTADO del garaje (cards de bicis con sus componentes).
 * Cambia únicamente si cambian las reglas de visualización del garaje
 * (orden, paginado, datos del card). No registra bicis ni instala piezas.
 */

/**
 * Controlador web responsable exclusivamente del listado del garaje (cards de bicicletas con sus componentes).
 * Gestiona la carga de bicicletas asociadas al ciclista autenticado y la vista principal del módulo.
 */

@Controller
@RequestMapping("/welcome/garaje")
public class GarajeController {

    @Autowired
    private BicicletaService bicicletaService;

/**
     * Muestra la vista principal del garaje con las bicicletas, componentes y alertas del usuario en sesión.
     *
     * @param session Sesión HTTP utilizada para validar si existe un usuario autenticado.
     * @param model Modelo de Spring UI para pasar los datos del usuario, el módulo activo y la lista de bicicletas a la vista Thymeleaf.
     * @return El nombre de la plantilla HTML 'welcome' si está autenticado, o redirección a '/login' si no hay sesión activa.
     */
    
    // GET /welcome/garaje — cards del usuario logueado.
    @GetMapping
    public String verGaraje(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("moduloActivo", "garaje");
        model.addAttribute("bicicletas", bicicletaService.listarConComponentes(usuario.getId_usuario()));
        // Flag para el HTML: solo el rol "taller" ve el editor de límite por km.
        model.addAttribute("esTaller", UsuarioService.esTaller(usuario));
        return "welcome";
    }
}
