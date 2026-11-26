package com.ProyectoBici.proyecto.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ProyectoBici.proyecto.dto.PanelResumen;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.service.PanelService;

import jakarta.servlet.http.HttpSession;

/**
 * ---------------------------------------------------------------------------
 * PANEL DE CONTROL — solo ADAPTACIÓN HTTP.
 * ---------------------------------------------------------------------------
 * - Intercepta GET /welcome/panelControl ANTES que la ruta genérica
 *   /welcome/{modulo} (Spring prefiere la ruta exacta sobre la variable).
 * - La agregación (km, alertas, bici elegida) vive en PanelService;
 *   aquí solo se valida sesión y se publica al Model.
 */
@Controller
@RequestMapping("/welcome/panelControl")
public class PanelControlController {

    // Agregación del panel (sumas, conteos, selección). Sin HTTP aquí dentro.
    @Autowired
    private PanelService panelService;

    /**
     * @param biciId  (opcional) bici del desplegable; si es null/ajena, la primera.
     */
    @GetMapping
    public String verPanel(
            @RequestParam(value = "biciId", required = false) Integer biciId,
            HttpSession session,
            Model model) {

        // Sin usuario en sesión, volver al login.
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        // Delegar el cálculo y publicar cada campo con el nombre que usa el HTML.
        PanelResumen resumen = panelService.armarPanel(usuario.getId_usuario(), biciId);
        model.addAttribute("usuario", usuario);
        model.addAttribute("moduloActivo", "panelControl"); // activa el bloque en welcome.html
        model.addAttribute("totalBicis", resumen.getTotalBicis());
        model.addAttribute("kmTotales", resumen.getKmTotales());
        model.addAttribute("totalAlertas", resumen.getTotalAlertas());
        model.addAttribute("bicicletas", resumen.getBicicletas());
        model.addAttribute("biciSeleccionada", resumen.getBiciSeleccionada());
        model.addAttribute("componentesSel", resumen.getComponentesSel());
        model.addAttribute("alertasSel", resumen.getAlertasSel());

        return "welcome";
    }
}
