package com.ProyectoBici.proyecto.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ProyectoBici.proyecto.dto.MetodoPagoForm;
import com.ProyectoBici.proyecto.entity.MetodoPago;
import com.ProyectoBici.proyecto.entity.TipoMetodoPago;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.service.MetodoPagoService;
import com.ProyectoBici.proyecto.shared.exception.BusinessException;
import com.ProyectoBici.proyecto.shared.exception.UnauthorizedException;
import com.ProyectoBici.proyecto.shared.message.AppMessage;
import com.ProyectoBici.proyecto.shared.message.FlashMessages;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Solo el registro de métodos de pago (formulario + guardado + baja).
 * Cambia únicamente si cambian las reglas de pago.
 *
 * Errores de validación -> se vuelve a pintar el formulario en la misma petición
 * (sin reenviar número de tarjeta ni CVV). Sin sesión -> UnauthorizedException (GlobalExceptionHandler).
 */
@Controller
@RequestMapping("/welcome/pagos")
@RequiredArgsConstructor
public class PagoController {

    private static final String MODULO = "pagos";

    private final MetodoPagoService metodoPagoService;

    // GET /welcome/pagos — formulario + métodos ya registrados.
    @GetMapping
    public String formulario(HttpSession session, Model model) {
        Usuario usuario = usuarioEnSesion(session);

        MetodoPagoForm form = new MetodoPagoForm();
        form.setTipo(TipoMetodoPago.TARJETA); // opción inicial
        prepararVista(usuario, form, model);
        return "welcome";
    }

    // POST /welcome/pagos — valida y registra el método elegido.
    @PostMapping
    public String registrar(
            @Valid @ModelAttribute("pagoForm") MetodoPagoForm form,
            BindingResult result,
            HttpSession session,
            Model model,
            RedirectAttributes redirect) {

        Usuario usuario = usuarioEnSesion(session);

        if (result.hasErrors()) {
            return volverConError(usuario, form, model, AppMessage.error(mensajeDe(result)));
        }

        try {
            MetodoPago guardado = metodoPagoService.registrar(usuario, form);
            FlashMessages.success(redirect, mensajeExito(guardado));
            return "redirect:/welcome/pagos";
        } catch (BusinessException e) {
            return volverConError(usuario, form, model, AppMessage.of(e.getType(), e.getMessage()));
        }
    }

    // POST /welcome/pagos/{id}/eliminar — solo si el método es del usuario logueado.
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") int id, HttpSession session, RedirectAttributes redirect) {
        Usuario usuario = usuarioEnSesion(session);
        metodoPagoService.eliminar(id, usuario.getId_usuario());
        FlashMessages.success(redirect, "Método de pago eliminado.");
        return "redirect:/welcome/pagos";
    }

    // ---------- Apoyo ----------

    private void prepararVista(Usuario usuario, MetodoPagoForm form, Model model) {
        model.addAttribute("usuario", usuario);
        model.addAttribute("moduloActivo", MODULO);
        model.addAttribute("pagoForm", form);
        model.addAttribute("metodosPago", metodoPagoService.listarDeUsuario(usuario.getId_usuario()));
    }

    private String volverConError(Usuario usuario, MetodoPagoForm form, Model model, AppMessage mensaje) {
        form.limpiarDatosSensibles();
        prepararVista(usuario, form, model);
        FlashMessages.now(model, mensaje);
        return "welcome";
    }

    private String mensajeDe(BindingResult result) {
        if (result.hasFieldErrors("tipo")) {
            return "Selecciona un método de pago válido.";
        }
        String mensaje = result.getAllErrors().get(0).getDefaultMessage();
        return mensaje != null ? mensaje : "Revisa los datos ingresados.";
    }

    private String mensajeExito(MetodoPago metodo) {
        if (metodo.getTipo().esBilletera()) {
            return "Pago con " + metodo.getTipo().getEtiqueta()
                    + " registrado. Quedó pendiente de verificación de la operación.";
        }
        return "Tarjeta " + metodo.getMarcaTarjeta() + " terminada en " + metodo.getUltimosDigitos() + " registrada.";
    }

    private Usuario usuarioEnSesion(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            throw new UnauthorizedException(); // el handler redirige a /login con un aviso
        }
        return usuario;
    }
}