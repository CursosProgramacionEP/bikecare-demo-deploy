package com.ProyectoBici.proyecto.dto;

import java.time.LocalDateTime;

/** Fila del panel /admin/pagos: un pago Plin/Yape esperando que el administrador lo apruebe. */
public record PagoPendienteAdmin(
        int idMetodoPago,
        String icono,
        String tipoEtiqueta,
        String numeroOperacion,
        String nombreUsuario,
        String correoUsuario,
        LocalDateTime fechaRegistro) {
}
