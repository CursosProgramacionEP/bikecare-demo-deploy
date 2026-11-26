package com.ProyectoBici.proyecto.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ProyectoBici.proyecto.dto.ComponenteInstalacionForm;
import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.service.BicicletaService;
import com.ProyectoBici.proyecto.service.ComponenteCatalogoService;
import com.ProyectoBici.proyecto.service.ComponenteInstalacionService;
import com.ProyectoBici.proyecto.shared.message.FlashMessages;

import jakarta.servlet.http.HttpSession;

/**
 * Solo la INSTALACIÓN de componentes en una bici
 * (formulario del catálogo + guardado). Cambia únicamente si cambian
 * las reglas de instalación (duplicados, km inicial, umbrales).
 * No lista el garaje ni registra bicis.
 */
@Controller
@RequestMapping("/welcome/garaje/{idBici}/componentes")
public class ComponenteInstalacionController {

    @Autowired
    private BicicletaService bicicletaService;

    @Autowired
    private ComponenteCatalogoService componenteCatalogoService;

    @Autowired
    private ComponenteInstalacionService componenteInstalacionService;

    // GET /welcome/garaje/{idBici}/componentes — catálogo + instalados.
    @GetMapping
    public String formularioComponentes(
            @PathVariable("idBici") int idBici,
            HttpSession session,
            Model model,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        // Solo se puede entrar a bicis propias
        Bicicleta bici = bicicletaService.buscarDeUsuario(idBici, usuario.getId_usuario()).orElse(null);
        if (bici == null) {
            FlashMessages.error(redirect, "No se encontró esa bicicleta.");
            return "redirect:/welcome/garaje";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("moduloActivo", "addComponentes");
        model.addAttribute("bicicleta", bici);
        model.addAttribute("catalogo", componenteCatalogoService.listarCatalogo());
        model.addAttribute("instalados", componenteCatalogoService.idsInstalados(bici.getIdBicicleta()));
        model.addAttribute("hoy", LocalDate.now());
        return "welcome";
    }

    // POST /welcome/garaje/{idBici}/componentes — guarda los marcados.
    @PostMapping
    public String guardarComponentes(
            @PathVariable("idBici") int idBici,
            @RequestParam(value = "componentes", required = false) List<Integer> componentes,
            @RequestParam Map<String, String> params, // marca_{id}, modelo_{id}, fecha_{id}, km_{id}
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        Bicicleta bici = bicicletaService.buscarDeUsuario(idBici, usuario.getId_usuario()).orElse(null);
        if (bici == null) {
            FlashMessages.error(redirect, "No se encontró esa bicicleta.");
            return "redirect:/welcome/garaje";
        }

        int guardados = componenteInstalacionService.agregarComponentes(bici,
                ComponenteInstalacionForm.desdeParams(componentes, params, bici.getKilometrajeTotal()));
        if (guardados == 0) {
            FlashMessages.warning(redirect, "No se añadió ningún componente nuevo.");
        } else {
            FlashMessages.success(redirect, guardados == 1
        ? "Se añadió 1 componente a " + bici.getMarca() + " " + bici.getModelo() + "."
        : "Se añadieron " + guardados + " componentes a " + bici.getMarca() + " " + bici.getModelo() + ".");
        }
        return "redirect:/welcome/garaje";
    }
}
