package com.ProyectoBici.proyecto.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ProyectoBici.proyecto.dto.ComprobantePago;
import com.ProyectoBici.proyecto.entity.MetodoPago;
import com.ProyectoBici.proyecto.repository.MetodoPagoRepository;
import com.ProyectoBici.proyecto.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Implementación por defecto del servicio de comprobantes de pago.
 * Construye el comprobante a partir de un método de pago registrado.
 */
@Service
@RequiredArgsConstructor
public class DefaultComprobantePagoService implements ComprobantePagoService {

    private final MetodoPagoRepository metodoPagoRepository;

    @Override
    @Transactional(readOnly = true)
    public ComprobantePago obtener(int idMetodo, int idUsuario) {
        MetodoPago metodo = metodoPagoRepository.buscarDeUsuario(idMetodo, idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró ese método de pago.", "/welcome/pagos"));

        ComprobantePago comprobante = new ComprobantePago();
        comprobante.setIdComprobante(metodo.getIdMetodoPago());
        comprobante.setNumeroComprobante(String.format("CP-%06d", metodo.getIdMetodoPago()));
        comprobante.setFechaEmision(java.time.LocalDateTime.now());
        comprobante.setNombreUsuario(metodo.getUsuario().getNombre() + " " + metodo.getUsuario().getApellidos());
        comprobante.setCorreoUsuario(metodo.getUsuario().getCorreo());
        comprobante.setTipoMetodo(metodo.getTipo().getEtiqueta());
        comprobante.setDetalle(detalleDe(metodo));
        comprobante.setEstado(metodo.getEstado().getEtiqueta());
        comprobante.setConcepto("Registro de método de pago");

        return comprobante;
    }

    private String detalleDe(MetodoPago metodo) {
        if (metodo.getTipo().esBilletera()) {
            return "Operación N° " + metodo.getNumeroOperacion();
        }
        return metodo.getMarcaTarjeta() + " **** " + metodo.getUltimosDigitos()
                + "  ·  vence " + String.format("%02d/%02d", metodo.getMesExpiracion(), metodo.getAnioExpiracion() % 100);
    }
}
