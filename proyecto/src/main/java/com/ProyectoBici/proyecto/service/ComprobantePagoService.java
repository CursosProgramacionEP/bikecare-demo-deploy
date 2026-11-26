package com.ProyectoBici.proyecto.service;

import com.ProyectoBici.proyecto.dto.ComprobantePago;

/**
 * Contrato para la generación de comprobantes de pago.
 * Permite cambiar la implementación (HTML, PDF, etc.) sin tocar el controlador.
 */
public interface ComprobantePagoService {

    /**
     * Construye el comprobante de pago asociado a un método de pago del usuario.
     *
     * @param idMetodo  identificador del método de pago.
     * @param idUsuario identificador del usuario autenticado.
     * @return comprobante con los datos listos para la vista.
     * @throws com.ProyectoBici.proyecto.shared.exception.ResourceNotFoundException
     *         si el método no existe o no pertenece al usuario.
     */
    ComprobantePago obtener(int idMetodo, int idUsuario);
}
