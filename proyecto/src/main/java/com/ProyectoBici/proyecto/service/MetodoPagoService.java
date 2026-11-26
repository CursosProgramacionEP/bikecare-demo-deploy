package com.ProyectoBici.proyecto.service;

import java.time.YearMonth;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ProyectoBici.proyecto.dto.MetodoPagoForm;
import com.ProyectoBici.proyecto.dto.MetodoPagoResumen;
import com.ProyectoBici.proyecto.entity.EstadoMetodoPago;
import com.ProyectoBici.proyecto.entity.MetodoPago;
import com.ProyectoBici.proyecto.entity.TipoMetodoPago;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.repository.MetodoPagoRepository;
import com.ProyectoBici.proyecto.shared.exception.BusinessException;
import com.ProyectoBici.proyecto.shared.exception.ResourceNotFoundException;
import com.ProyectoBici.proyecto.shared.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Registro y procesamiento de métodos de pago (HU-14).
 *
 * - TARJETA: valida (Luhn, titular, vencimiento, CVV) y guarda SOLO marca + últimos 4 + titular + vencimiento.
 *   El número completo y el CVV se descartan al terminar el método (no hay pasarela de pago real).
 * - PLIN / YAPE: valida y guarda el número de operación; queda PENDIENTE_VERIFICACION.
 *
 * Los errores esperables se lanzan como BusinessException (el mensaje se muestra tal cual al usuario).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetodoPagoService {

    private static final int TARJETA_MIN_DIGITOS = 13;
    private static final int TARJETA_MAX_DIGITOS = 19;
    private static final int TARJETA_MAX_ANIOS_VIGENCIA = 20;

    private enum MarcaTarjeta {
        VISA, MASTERCARD, AMEX, OTRA
    }

    private final MetodoPagoRepository metodoPagoRepository;

    // ======================================================================
    // Registro
    // ======================================================================

    @Transactional
    public MetodoPago registrar(Usuario usuario, MetodoPagoForm form) {
        if (usuario == null) {
            throw new UnauthorizedException();
        }
        if (form == null || form.getTipo() == null) {
            throw new BusinessException("Selecciona un método de pago.");
        }

        MetodoPago metodo = new MetodoPago();
        metodo.setUsuario(usuario);
        metodo.setTipo(form.getTipo());

        if (form.getTipo() == TipoMetodoPago.TARJETA) {
            procesarTarjeta(metodo, form, usuario.getId_usuario());
        } else {
            procesarBilletera(metodo, form);
        }

        try {
            MetodoPago guardado = metodoPagoRepository.saveAndFlush(metodo);
            // Nunca se loguea número de tarjeta, CVV ni número de operación.
            log.info("Método de pago {} registrado (id={}) para el usuario {}", guardado.getTipo(),
                    guardado.getIdMetodoPago(), usuario.getId_usuario());
            return guardado;
        } catch (DataIntegrityViolationException e) {
            // Carrera: otro request guardó el mismo número de operación justo antes.
            throw new BusinessException("Ese número de operación ya fue registrado.");
        }
    }

    // ---------- TARJETA ----------

    private void procesarTarjeta(MetodoPago metodo, MetodoPagoForm form, int idUsuario) {
        String numero = soloDigitos(form.getNumeroTarjeta());
        if (numero.isEmpty()) {
            throw new BusinessException("Ingresa el número de tarjeta.");
        }
        if (numero.length() < TARJETA_MIN_DIGITOS || numero.length() > TARJETA_MAX_DIGITOS || !cumpleLuhn(numero)) {
            throw new BusinessException("El número de tarjeta no es válido.");
        }

        String titular = normalizarTexto(form.getTitular());
        if (titular.length() < 3) {
            throw new BusinessException("Ingresa el nombre del titular tal como figura en la tarjeta.");
        }

        YearMonth vencimiento = parsearExpiracion(form.getExpiracion());
        YearMonth hoy = YearMonth.now();
        if (vencimiento.isBefore(hoy)) {
            throw new BusinessException("La tarjeta está vencida.");
        }
        if (vencimiento.isAfter(hoy.plusYears(TARJETA_MAX_ANIOS_VIGENCIA))) {
            throw new BusinessException("La fecha de expiración no es válida.");
        }

        MarcaTarjeta marca = detectarMarca(numero);
        int largoCvv = marca == MarcaTarjeta.AMEX ? 4 : 3;
        String cvv = form.getCvv() == null ? "" : form.getCvv().trim();
        if (!cvv.matches("[0-9]{" + largoCvv + "}")) {
            throw new BusinessException("El CVV debe tener " + largoCvv + " dígitos.");
        }
        // El CVV solo se valida: no se asigna a la entidad ni se persiste.

        String ultimos = numero.substring(numero.length() - 4);
        if (metodoPagoRepository.existeTarjeta(idUsuario, TipoMetodoPago.TARJETA, marca.name(), ultimos,
                vencimiento.getMonthValue(), vencimiento.getYear())) {
            throw new BusinessException("Esa tarjeta ya está registrada en tu cuenta.");
        }

        metodo.setTitular(titular.toUpperCase());
        metodo.setMarcaTarjeta(marca.name());
        metodo.setUltimosDigitos(ultimos);
        metodo.setMesExpiracion(vencimiento.getMonthValue());
        metodo.setAnioExpiracion(vencimiento.getYear());
        metodo.setEstado(EstadoMetodoPago.ACTIVO);
    }

    // ---------- PLIN / YAPE ----------

    private void procesarBilletera(MetodoPago metodo, MetodoPagoForm form) {
        TipoMetodoPago tipo = form.getTipo();
        String operacion = form.getNumeroOperacion() == null ? "" : form.getNumeroOperacion().trim().toUpperCase();

        if (operacion.isEmpty()) {
            throw new BusinessException("Ingresa el número de operación de tu pago con " + tipo.getEtiqueta() + ".");
        }
        if (!operacion.matches("[A-Z0-9]{6,20}")) {
            throw new BusinessException("El número de operación debe tener entre 6 y 20 letras o números.");
        }
        if (metodoPagoRepository.existsByTipoAndNumeroOperacion(tipo, operacion)) {
            throw new BusinessException("Ese número de operación ya fue registrado.");
        }

        metodo.setNumeroOperacion(operacion);
        metodo.setEstado(EstadoMetodoPago.PENDIENTE_VERIFICACION);
    }

    // ======================================================================
    // Consulta / baja
    // ======================================================================

    @Transactional(readOnly = true)
    public List<MetodoPagoResumen> listarDeUsuario(int idUsuario) {
        return metodoPagoRepository.listarDeUsuario(idUsuario).stream().map(this::aResumen).toList();
    }

    @Transactional
    public void eliminar(int idMetodo, int idUsuario) {
        MetodoPago metodo = metodoPagoRepository.buscarDeUsuario(idMetodo, idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ese método de pago.", "/welcome/pagos"));
        metodoPagoRepository.delete(metodo);
        log.info("Método de pago {} eliminado por el usuario {}", idMetodo, idUsuario);
    }

    // ======================================================================
    // Utilidades
    // ======================================================================

    private MetodoPagoResumen aResumen(MetodoPago m) {
        String detalle;
        if (m.getTipo() == TipoMetodoPago.TARJETA) {
            detalle = m.getMarcaTarjeta() + " •••• " + m.getUltimosDigitos()
                    + " · vence " + String.format("%02d/%02d", m.getMesExpiracion(), m.getAnioExpiracion() % 100)
                    + " · " + m.getTitular();
        } else {
            detalle = "Operación N° " + m.getNumeroOperacion();
        }
        return new MetodoPagoResumen(m.getIdMetodoPago(), m.getTipo().getIcono(), m.getTipo().getEtiqueta(), detalle,
                m.getEstado().getEtiqueta(), m.getEstado().getCss(), m.getFechaRegistro());
    }

    private static String soloDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    private static String normalizarTexto(String texto) {
        return texto == null ? "" : texto.trim().replaceAll("\\s+", " ");
    }

    /** MM/AA -> YearMonth (siglo 2000). */
    private static YearMonth parsearExpiracion(String expiracion) {
        if (expiracion == null || !expiracion.trim().matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
            throw new BusinessException("La fecha de expiración debe tener el formato MM/AA.");
        }
        String[] partes = expiracion.trim().split("/");
        return YearMonth.of(2000 + Integer.parseInt(partes[1]), Integer.parseInt(partes[0]));
    }

    /** Algoritmo de Luhn: detecta errores de tipeo en el número de tarjeta. */
    private static boolean cumpleLuhn(String numero) {
        int suma = 0;
        boolean duplicar = false;
        for (int i = numero.length() - 1; i >= 0; i--) {
            int digito = numero.charAt(i) - '0';
            if (duplicar) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }
            suma += digito;
            duplicar = !duplicar;
        }
        return suma % 10 == 0;
    }

    private static MarcaTarjeta detectarMarca(String numero) {
        if (numero.startsWith("4")) {
            return MarcaTarjeta.VISA;
        }
        if (numero.startsWith("34") || numero.startsWith("37")) {
            return MarcaTarjeta.AMEX;
        }
        int dos = Integer.parseInt(numero.substring(0, 2));
        int cuatro = Integer.parseInt(numero.substring(0, 4));
        if ((dos >= 51 && dos <= 55) || (cuatro >= 2221 && cuatro <= 2720)) {
            return MarcaTarjeta.MASTERCARD;
        }
        return MarcaTarjeta.OTRA;
    }
}
