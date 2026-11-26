package com.ProyectoBici.proyecto.shared.report;

import java.nio.charset.StandardCharsets;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Reporte ya generado: nombre de archivo, tipo de contenido y bytes. */
@Getter
@AllArgsConstructor
public class GeneratedReport {

    private final String fileName;
    private final String contentType;
    private final byte[] content;

    /** Respuesta lista para devolver desde un controlador (descarga del archivo). */
    public ResponseEntity<byte[]> asDownload() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(content.length)
                .body(content);
    }
}
