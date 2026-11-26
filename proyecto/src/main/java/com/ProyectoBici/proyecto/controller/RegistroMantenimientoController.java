package com.ProyectoBici.proyecto.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.BicicletaComponente;
import com.ProyectoBici.proyecto.entity.Mantenimiento;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.service.RegistroMantenimientoService;
import com.ProyectoBici.proyecto.shared.message.FlashMessages;

import jakarta.servlet.http.HttpSession;

/**
 * Controlador web del módulo Registro de Mantenimiento. Expone el formulario
 * con las bicicletas del usuario y delega las reglas de negocio en
 * {@link RegistroMantenimientoService}.
 */
@Controller
@RequestMapping("/welcome/mantenimiento")
public class RegistroMantenimientoController {

    /** Cantidad de mantenimientos recientes que se muestran en el modulo. */
    private static final int ULTIMOS_MANTENIMIENTOS = 2;

    @Autowired
    private RegistroMantenimientoService mantenimientoService;

    /**
     * Muestra el formulario de registro y los mantenimientos ya guardados por
     * el usuario autenticado.
     *
     * @param session sesión HTTP de donde se obtiene el usuario logueado.
     * @param model modelo de la vista con bicicletas, mantenimientos y fecha actual.
     * @return {@code "welcome"} si hay sesión, o {@code "redirect:/login"} si no.
     */
    @GetMapping
    public String verFormulario(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        List<Bicicleta> bicicletas = mantenimientoService.listarBicisDeUsuario(usuario.getId_usuario());
        List<Mantenimiento> mantenimientos = mantenimientoService
                .listarUltimosMantenimientos(usuario.getId_usuario(), ULTIMOS_MANTENIMIENTOS);

        Map<Integer, List<BicicletaComponente>> componentesPorBici = new LinkedHashMap<>();
        for (Bicicleta bici : bicicletas) {
            componentesPorBici.put(bici.getIdBicicleta(),
                    mantenimientoService.listarComponentesActivos(bici.getIdBicicleta()));
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("moduloActivo", "mantenimiento");
        model.addAttribute("bicicletas", bicicletas);
        model.addAttribute("mantenimientos", mantenimientos);
        model.addAttribute("componentesPorBici", componentesPorBici);
        model.addAttribute("hoy", LocalDate.now());

        return "welcome";
    }

    /**
     * Valida y guarda un mantenimiento. Si el servicio rechaza algún dato,
     * vuelve al formulario con el mensaje de error correspondiente.
     *
     * @param idBicicleta bicicleta seleccionada en el formulario.
     * @param fechaServicio fecha del servicio en formato ISO (yyyy-MM-dd).
     * @param tipoServicio tipo de mantenimiento realizado.
     * @param kilometrajeServicio kilometraje del odómetro al momento del servicio.
     * @param costo costo del servicio (opcional).
     * @param tallerMecanico taller donde se realizó el servicio (opcional).
     * @param descripcion notas del servicio (opcional).
     * @param session sesión HTTP para verificar la autenticación.
     * @param redirect atributos flash para el mensaje de resultado.
     * @return redirección al formulario del módulo.
     */
    @PostMapping
    public String guardar(
            @RequestParam(value = "idBicicleta", required = false) Integer idBicicleta,
            @RequestParam(value = "fechaServicio", required = false) String fechaServicio,
            @RequestParam(value = "tipoServicio", required = false) String tipoServicio,
            @RequestParam(value = "kilometrajeServicio", required = false) String kilometrajeServicio,
            @RequestParam(value = "costo", required = false) String costo,
            @RequestParam(value = "tallerMecanico", required = false) String tallerMecanico,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "componentes", required = false) List<Integer> componentes,
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            Mantenimiento guardado = mantenimientoService.registrar(usuario.getId_usuario(), idBicicleta,
                    fechaServicio, tipoServicio, kilometrajeServicio, costo, tallerMecanico, descripcion,
                    componentes);

            String nombreBici = guardado.getBicicleta().getMarca() + " " + guardado.getBicicleta().getModelo();
            FlashMessages.success(redirect, "Mantenimiento de " + guardado.getTipoServicio()
                    + " registrado en " + nombreBici + ".");
        } catch (IllegalArgumentException e) {
            FlashMessages.error(redirect, e.getMessage());
        }

        return "redirect:/welcome/mantenimiento";
    }
}
