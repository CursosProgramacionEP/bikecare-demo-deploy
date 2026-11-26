package com.ProyectoBici.proyecto.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ProyectoBici.proyecto.dto.DiagnosticoBiciContexto;
import com.ProyectoBici.proyecto.dto.DiagnosticoResultado;
import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.repository.BicicletaRepository;
import com.ProyectoBici.proyecto.service.DiagnosticoIAService;
import com.ProyectoBici.proyecto.service.GeminiDiagnosticoService;
import com.ProyectoBici.proyecto.service.UsuarioService;
import com.ProyectoBici.proyecto.shared.message.AppMessage;
import com.ProyectoBici.proyecto.shared.message.FlashMessages;

import jakarta.servlet.http.HttpSession;

/**
 * Solo ADAPTACIÓN HTTP del módulo Diagnóstico IA.
 * GET muestra el formulario (bicis del usuario); POST valida e intenta
 * Gemini primero ({@link GeminiDiagnosticoService}) con fallback a las
 * reglas locales ({@link DiagnosticoIAService}). La foto/audio se
 * reciben pero aún no se procesan (solo visual).
 */
@Controller
@RequestMapping("/welcome/diagnosticoIA")
public class DiagnosticoIAController {

    @Autowired
    private BicicletaRepository bicicletaRepository;

    @Autowired
    private DiagnosticoIAService diagnosticoIAService;

    @Autowired
    private GeminiDiagnosticoService geminiDiagnosticoService;

    @Autowired
    private UsuarioService usuarioService;

    // GET: formulario vertical (entrada arriba, resultados abajo vacíos).
    @GetMapping
    public String verDiagnostico(
            @RequestParam(value = "biciId", required = false) Integer biciId,
            HttpSession session, Model model, RedirectAttributes redirect) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }
        if (!usuarioService.esPremium(usuario.getId_usuario())) {
            return bloquearSinPremium(redirect);
        }
        List<Bicicleta> bicis = bicicletaRepository.listarDeUsuario(usuario.getId_usuario());
        model.addAttribute("usuario", usuario);
        model.addAttribute("moduloActivo", "diagnosticoIA");
        model.addAttribute("bicicletas", bicis);
        model.addAttribute("biciSeleccionada", seleccionar(bicis, biciId));
        model.addAttribute("mostrarResultado", false);
        return "welcome";
    }

    // POST: procesa el formulario y devuelve el mismo módulo con el resultado.
    @PostMapping("/analizar")
    public String analizar(
            @RequestParam(value = "idBicicleta", required = false) Integer idBici,
            @RequestParam(value = "componentes", required = false) List<String> componentes,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            @RequestParam(value = "audio", required = false) MultipartFile audio,
            HttpSession session, Model model, RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }
        if (!usuarioService.esPremium(usuario.getId_usuario())) {
            return bloquearSinPremium(redirect);
        }
        if (idBici == null) {
            FlashMessages.error(redirect, "Selecciona la bicicleta a evaluar.");
            return "redirect:/welcome/diagnosticoIA";
        }
        Bicicleta bici = bicicletaRepository.buscarDeUsuario(idBici, usuario.getId_usuario()).orElse(null);
        if (bici == null) {
            FlashMessages.error(redirect, "No se encontró esa bicicleta.");
            return "redirect:/welcome/diagnosticoIA";
        }
        if (descripcion != null && descripcion.trim().length() > 2000) {
            FlashMessages.error(redirect, "La descripción no puede pasar de 2000 caracteres.");
            return "redirect:/welcome/diagnosticoIA?biciId=" + idBici;
        }
        boolean hayTexto = descripcion != null && !descripcion.isBlank();
        boolean hayFoto = foto != null && !foto.isEmpty();
        boolean hayAudio = audio != null && !audio.isEmpty();
        // Sin evidencia no hay nada que diagnosticar (componentes solos no bastan).
        if (!hayTexto && !hayFoto && !hayAudio) {
            FlashMessages.error(redirect, "Describe los síntomas o adjunta una foto o un audio para analizar.");
            return "redirect:/welcome/diagnosticoIA?biciId=" + idBici;
        }
        if (hayFoto && !esFotoValida(foto)) {
            FlashMessages.error(redirect, "La foto debe ser JPG o PNG de máximo 5 MB.");
            return "redirect:/welcome/diagnosticoIA?biciId=" + idBici;
        }
        if (hayAudio && !esAudioValido(audio)) {
            FlashMessages.error(redirect, "El audio debe ser un archivo de audio de máximo 5 MB.");
            return "redirect:/welcome/diagnosticoIA?biciId=" + idBici;
        }
        // Texto para la IA: si solo hay adjuntos, indicarlo para que no llegue vacío.
        String texto = hayTexto ? descripcion.trim() : "(sin descripción escrita; basarse en los adjuntos)";

        DiagnosticoResultado resultado;
        String fuente;
        if (geminiDiagnosticoService.estaConfigurado()) {
            try {
                resultado = geminiDiagnosticoService.diagnosticarConGemini(
                        DiagnosticoBiciContexto.desde(bici), componentes, texto, foto, audio);
                fuente = "gemini";
            } catch (Exception e) {
                resultado = diagnosticoIAService.diagnosticar(texto, componentes);
                fuente = "local";
                FlashMessages.now(model,
                        AppMessage.warning("Gemini no respondió, se usó el diagnóstico local."));
            }
        } else {
            resultado = diagnosticoIAService.diagnosticar(texto, componentes);
            fuente = "local";
        }

        List<Bicicleta> bicis = bicicletaRepository.listarDeUsuario(usuario.getId_usuario());
        model.addAttribute("usuario", usuario);
        model.addAttribute("moduloActivo", "diagnosticoIA");
        model.addAttribute("bicicletas", bicis);
        model.addAttribute("biciSeleccionada", bici);
        model.addAttribute("descripcion", hayTexto ? descripcion.trim() : "");
        model.addAttribute("componentesSel", componentes == null ? List.of() : componentes);
        model.addAttribute("fotoNombre", hayFoto ? foto.getOriginalFilename() : null);
        model.addAttribute("audioNombre", hayAudio ? audio.getOriginalFilename() : null);
        model.addAttribute("resultado", resultado);
        model.addAttribute("fuenteDiagnostico", fuente);
        model.addAttribute("mostrarResultado", true);
        return "welcome";
    }

    private static final long MAX_ADJUNTO_BYTES = 5L * 1024 * 1024;

    private boolean esFotoValida(MultipartFile foto) {
        if (foto.getSize() > MAX_ADJUNTO_BYTES) {
            return false;
        }
        String tipo = foto.getContentType() == null ? "" : foto.getContentType().toLowerCase();
        return tipo.equals("image/jpeg") || tipo.equals("image/png");
    }

    private boolean esAudioValido(MultipartFile audio) {
        if (audio.getSize() > MAX_ADJUNTO_BYTES) {
            return false;
        }
        String tipo = audio.getContentType() == null ? "" : audio.getContentType().toLowerCase();
        return tipo.startsWith("audio/");
    }

    private Bicicleta seleccionar(List<Bicicleta> bicis, Integer biciId) {        if (bicis.isEmpty()) {
            return null;
        }
        if (biciId != null) {
            for (Bicicleta b : bicis) {
                if (b.getIdBicicleta() == biciId) {
                    return b;
                }
            }
        }
        return bicis.get(0);
    }

    /** El módulo es premium: se desbloquea cuando el administrador aprueba el pago del usuario. */
    private String bloquearSinPremium(RedirectAttributes redirect) {
        FlashMessages.warning(redirect,
                "El Diagnóstico IA es premium. Registra tu pago con Yape o Plin y se activará cuando el administrador lo apruebe.");
        return "redirect:/welcome/pagos";
    }
}
