package com.ProyectoBici.proyecto.dto;

import com.ProyectoBici.proyecto.entity.TipoMetodoPago;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

/**
 * Datos que llegan del formulario "Métodos de pago".
 * Los nombres coinciden con los name="" de los inputs del HTML.
 *
 * Aquí solo se valida el FORMATO. Las reglas que dependen del tipo elegido
 * (tarjeta obligatoria si TARJETA, operación obligatoria si PLIN/YAPE, Luhn,
 * vencimiento, duplicados...) las aplica MetodoPagoService.
 */
@Data
public class MetodoPagoForm {

    @NotNull(message = "Selecciona un método de pago.")
    private TipoMetodoPago tipo;

    // Datos sensibles: excluidos de toString() para que nunca acaben en un log.
    @ToString.Exclude
    @Pattern(regexp = "^[0-9 ]{0,23}$", message = "El número de tarjeta solo puede contener dígitos.")
    private String numeroTarjeta;

    @Size(max = 100, message = "El titular no puede superar los 100 caracteres.")
    @Pattern(regexp = "^[\\p{L} .'-]*$", message = "El titular solo puede contener letras y espacios.")
    private String titular;

    // Formato MM/AA
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$|^$", message = "La fecha de expiración debe tener el formato MM/AA.")
    private String expiracion;

    @ToString.Exclude
    @Pattern(regexp = "^[0-9]{0,4}$", message = "El CVV solo puede contener dígitos.")
    private String cvv;

    @Pattern(regexp = "^[A-Za-z0-9]{0,20}$", message = "El número de operación solo admite letras y números (máx. 20).")
    private String numeroOperacion;

    /** Se llama antes de volver a pintar el formulario con errores: el PAN y el CVV no se reenvían al navegador. */
    public void limpiarDatosSensibles() {
        this.numeroTarjeta = null;
        this.cvv = null;
    }
}
