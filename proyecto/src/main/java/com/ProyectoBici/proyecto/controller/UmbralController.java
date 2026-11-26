package com.ProyectoBici.proyecto.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.entity.Umbral;
import com.ProyectoBici.proyecto.service.UmbralService;
import com.ProyectoBici.proyecto.service.UsuarioService;
import com.ProyectoBici.proyecto.shared.message.FlashMessages;

import jakarta.servlet.http.HttpSession;

/**
 * ---------------------------------------------------------------------------
 * Edición del límite por kilometraje — SOLO rol Taller (adaptación HTTP).
 * ---------------------------------------------------------------------------
 * - Solo valida sesión/rol y convierte el texto del form a número.
 * - La propiedad ("¿es de mis bicis?") y el rango (50..100000) los valida
 *   UmbralService.actualizarLimiteKm, que lanza IllegalArgumentException.
 */
@Controller
@RequestMapping("/welcome/componentes")
public class UmbralController {

    @Autowired
    private UmbralService umbralService;

    /**
     * Guarda el nuevo límite en km de un componente instalado.
     * Llega desde el mini-form del card del Garaje (solo visible para Taller).
     */
    @PostMapping("/{idBc}/umbral")
    public String guardarLimite(
            @PathVariable("idBc") int idBc,
            @RequestParam(value = "limiteKm", required = false) String limiteRaw,
            HttpSession session,
            RedirectAttributes redirect) {

        // Debe haber login.
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        // Solo el rol "taller" puede editar límites.
        if (!UsuarioService.esTaller(usuario)) {
            FlashMessages.error(redirect, "Solo el rol Taller puede editar el límite por kilometraje.");
            return "redirect:/welcome/garaje";
        }

        // Adaptación HTTP: texto del input -> número (el rango lo valida el servicio).
        BigDecimal limite;
        try {
            if (limiteRaw == null || limiteRaw.isBlank()) {
                throw new NumberFormatException();
            }
            limite = new BigDecimal(limiteRaw.trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            FlashMessages.error(redirect, "El límite en km no es válido.");
            return "redirect:/welcome/garaje";
        }

        try {
            Umbral guardado = umbralService.actualizarLimiteKm(usuario.getId_usuario(), idBc, limite);
            FlashMessages.success(redirect,
                    "Límite actualizado a " + limite.stripTrailingZeros().toPlainString()
                            + " km en " + guardado.getBicicletaComponente().getComponente().getNombre() + ".");
        } catch (IllegalArgumentException e) {
            FlashMessages.error(redirect, e.getMessage());
        }
        return "redirect:/welcome/garaje";
    }

}
