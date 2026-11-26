package com.ProyectoBici.proyecto.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import com.ProyectoBici.proyecto.dto.DiagnosticoBiciContexto;
import com.ProyectoBici.proyecto.dto.DiagnosticoResultado;
import com.ProyectoBici.proyecto.dto.DiagnosticoResultado.Paso;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Cliente HTTP de Gemini para el diagnóstico preliminar.
 * Recibe bici + componentes + síntomas, pide a Gemini un JSON estricto
 * y lo convierte a {@link DiagnosticoResultado}. Si la key no está
 * configurada o la API falla, lanza excepción para que el controlador
 * use el fallback local ({@link DiagnosticoIAService}).
 *
 * <p>
 * Configuración en application.properties:
 * 
 * <pre>
 * gemini.api.key=${GEMINI_API_KEY:}
 * gemini.model=gemini-2.5-flash
 * </pre>
 */
@Service
public class GeminiDiagnosticoService {

    private static final Logger log = LoggerFactory.getLogger(GeminiDiagnosticoService.class);
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={key}";

    // Adjuntos opcionales: se envían a Gemini como inline_data (base64).
    private static final long MAX_ADJUNTO_BYTES = 5L * 1024 * 1024; // 5 MB c/u (igual que spring.servlet.multipart)
    private static final Set<String> TIPOS_FOTO = Set.of("image/jpeg", "image/png");
    private static final Set<String> PREFIJOS_AUDIO = Set.of("audio/");

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    /** true si hay key configurada (variable de entorno GEMINI_API_KEY). */
    public boolean estaConfigurado() {
        return apiKey != null && !apiKey.isBlank();
    }

    public DiagnosticoResultado diagnosticarConGemini(String biciInfo, List<String> componentes, String descripcion) {
        return diagnosticarConGemini(biciInfo, componentes, descripcion, null, null);
    }

    /** Sobrecarga con DTO: evita exponer la entidad completa, solo viaja lo esencial. */
    public DiagnosticoResultado diagnosticarConGemini(DiagnosticoBiciContexto bici, List<String> componentes,
            String descripcion, MultipartFile foto, MultipartFile audio) {
        return diagnosticarConGemini(bici == null ? "no especificada" : bici.toPrompt(),
                componentes, descripcion, foto, audio);
    }

    /**
     * Diagnóstico multimodal: texto siempre + foto y/o audio opcionales.
     * Los adjuntos inválidos (tipo o tamaño) se ignoran con un warning,
     * sin tumbar el diagnóstico de texto.
     */
    public DiagnosticoResultado diagnosticarConGemini(String biciInfo, List<String> componentes,
            String descripcion, MultipartFile foto, MultipartFile audio) {
        if (!estaConfigurado()) {
            throw new IllegalStateException("Falta GEMINI_API_KEY.");
        }
        String comps = (componentes == null || componentes.isEmpty())
                ? "no especificados"
                : String.join(", ", componentes);
        boolean conFoto = esAdjuntoValido(foto, true);
        boolean conAudio = esAdjuntoValido(audio, false);
        String promptUsuario = """
                Contexto de la bicicleta:
                %s
                Componentes marcados: %s
                Síntomas: %s
                %s%sDevuelve SOLO el JSON pedido, sin markdown ni texto extra.
                """.formatted(biciInfo == null ? "no especificada" : biciInfo, comps, descripcion,
                        conFoto ? "Se adjunta 1 foto de la zona afectada: analízala visualmente. " : "",
                        conAudio ? "Se adjunta 1 audio del ruido: considera su descripción. " : "");

        List<Object> parts = new ArrayList<>();
        parts.add(Map.of("text", promptUsuario));
        agregarAdjunto(parts, foto, true);
        agregarAdjunto(parts, audio, false);

        Map<String, Object> body = Map.of(
                "system_instruction", Map.of("parts", List.of(Map.of("text",
                        """
                                Eres un mecánico experto en bicicletas de la plataforma BikeCare.
                                Das diagnósticos preliminares ORIENTATIVOS en español, sin afirmar certeza absoluta.
                                Si se adjunta foto, describe lo que ves y úsalo como evidencia; si se adjunta audio, considera el tipo de ruido descrito.
                                Severidad: BAJO = uso normal con mantenimiento pronto; MEDIO = precaución, servicio esta semana; ALTO = no rodar, ir a taller.
                                Responde SOLO con este JSON válido (sin ``` ni comentarios):
                                {"severidad":"BAJO|MEDIO|ALTO","etiqueta":"Bajo|Media|Alta","mensajeUso":"...","causaTitulo":"...","causaDetalle":"...","soluciones":[{"titulo":"...","detalle":"..."},{"titulo":"...","detalle":"..."},{"titulo":"...","detalle":"..."}]}
                                Reglas: 3 soluciones como máximo, tono claro para ciclista no experto, incluye consejo de seguridad en mensajeUso.
                                """))),
                "generationConfig", Map.of(
                        "temperature", 0.3,
                        "maxOutputTokens", 2048,
                        "responseMimeType", "application/json",
                        "thinkingConfig", Map.of("thinkingBudget", 0)),
                "contents", List.of(Map.of("role", "user", "parts", parts)));

        String raw = null;
        int maxIntentos = 3;
        for (int intento = 1; intento <= maxIntentos; intento++) {
            try {
                raw = restClient.post()
                        .uri(ENDPOINT, model, apiKey)
                        .body(body)
                        .retrieve()
                        .body(String.class);
                break; // éxito
            } catch (org.springframework.web.client.HttpServerErrorException
                    | org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
                log.warn("Gemini: intento {}/{} falló: {}", intento, maxIntentos, e.getStatusCode());
                if (intento == maxIntentos) {
                    throw new IllegalStateException("Gemini no respondió: " + e.getMessage(), e);
                }
                try {
                    Thread.sleep(1500L * intento); // espera 1.5 s, luego 3 s
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrumpido", ie);
                }
            } catch (Exception e) {
                log.warn("Gemini: error HTTP: {}", e.getMessage());
                throw new IllegalStateException("Gemini no respondió: " + e.getMessage(), e);
            }
        }
        return parsear(raw);
    }

    // ---------- adjuntos (foto / audio) ----------

    private boolean esAdjuntoValido(MultipartFile archivo, boolean esFoto) {
        if (archivo == null || archivo.isEmpty()) {
            return false;
        }
        if (archivo.getSize() > MAX_ADJUNTO_BYTES) {
            log.warn("Gemini: adjunto ignorado por tamaño ({} bytes).", archivo.getSize());
            return false;
        }
        String tipo = archivo.getContentType() == null ? "" : archivo.getContentType().toLowerCase();
        if (esFoto) {
            return TIPOS_FOTO.contains(tipo);
        }
        return PREFIJOS_AUDIO.stream().anyMatch(tipo::startsWith);
    }

    /** Codifica a base64 y lo agrega como inline_data; si falla, se ignora sin tumbar el diagnóstico. */
    private void agregarAdjunto(List<Object> parts, MultipartFile archivo, boolean esFoto) {
        if (!esAdjuntoValido(archivo, esFoto)) {
            return;
        }
        try {
            String base64 = Base64.getEncoder().encodeToString(archivo.getBytes());
            String tipo = archivo.getContentType();
            parts.add(Map.of("inline_data", Map.of("mime_type", tipo, "data", base64)));
        } catch (Exception e) {
            log.warn("Gemini: no se pudo leer el adjunto, se continúa solo con texto.");
        }
    }

    // ---------- parseo y saneado ----------

    private DiagnosticoResultado parsear(String raw) {
        try {
            JsonNode root = mapper.readTree(raw);
            JsonNode texts = root.path("candidates").path(0).path("content").path("parts");
            StringBuilder sb = new StringBuilder();
            for (JsonNode p : texts) {
                sb.append(p.path("text").asText(""));
            }
            String json = extraerJson(sb.toString());
            JsonNode d = mapper.readTree(json);

            String severidad = d.path("severidad").asText("BAJO").trim().toUpperCase();
            if (!severidad.equals("BAJO") && !severidad.equals("MEDIO") && !severidad.equals("ALTO")) {
                severidad = "BAJO";
            }
            String etiqueta = switch (severidad) {
                case "ALTO" -> "Alta";
                case "MEDIO" -> "Media";
                default -> "Bajo";
            };
            String mensajeUso = recortar(d.path("mensajeUso").asText(""), 300);
            String causaTitulo = recortar(d.path("causaTitulo").asText("Diagnóstico preliminar"), 120);
            String causaDetalle = recortar(d.path("causaDetalle").asText(""), 800);

            List<Paso> pasos = new ArrayList<>();
            for (JsonNode s : d.path("soluciones")) {
                if (pasos.size() >= 3) {
                    break;
                }
                String t = recortar(s.path("titulo").asText(""), 120);
                String det = recortar(s.path("detalle").asText(""), 400);
                if (!t.isBlank()) {
                    pasos.add(new Paso(t, det));
                }
            }
            if (pasos.isEmpty()) {
                throw new IllegalStateException("Gemini devolvió un JSON sin soluciones.");
            }
            return new DiagnosticoResultado(severidad, etiqueta, mensajeUso, causaTitulo, causaDetalle, pasos);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Gemini: respuesta no parseable: {}", e.getMessage());
            throw new IllegalStateException("Respuesta de Gemini no válida.", e);
        }
    }

    /** Quita fences ```json y recorta al primer {...} válido. */
    private String extraerJson(String texto) {
        String t = texto == null ? ""
                : texto.trim()
                        .replaceAll("(?s)^```(?:json)?\\s*", "")
                        .replaceAll("(?s)\\s*```$", "")
                        .trim();
        int ini = t.indexOf('{');
        int fin = t.lastIndexOf('}');
        if (ini < 0 || fin <= ini) {
            throw new IllegalStateException("Gemini no devolvió JSON.");
        }
        return t.substring(ini, fin + 1);
    }

    private String recortar(String s, int max) {
        if (s == null) {
            return "";
        }
        s = s.trim();
        return s.length() <= max ? s : s.substring(0, max).trim();
    }
}
