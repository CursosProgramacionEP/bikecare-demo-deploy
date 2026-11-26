package com.ProyectoBici.proyecto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ProyectoBici.proyecto.dto.ComprobantePago;
import com.ProyectoBici.proyecto.entity.EstadoMetodoPago;
import com.ProyectoBici.proyecto.entity.MetodoPago;
import com.ProyectoBici.proyecto.entity.TipoMetodoPago;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.repository.MetodoPagoRepository;
import com.ProyectoBici.proyecto.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class ComprobantePagoServiceTest {

    private static final int ID_METODO = 7;
    private static final int ID_USUARIO = 1;

    @Mock
    private MetodoPagoRepository metodoPagoRepository;

    @InjectMocks
    private DefaultComprobantePagoService comprobanteService;

    @Test
    public void testObtenerTarjetaDevuelveComprobanteCompleto() {
        MetodoPago metodo = metodoTarjeta();
        when(metodoPagoRepository.buscarDeUsuario(ID_METODO, ID_USUARIO)).thenReturn(Optional.of(metodo));

        ComprobantePago comprobante = comprobanteService.obtener(ID_METODO, ID_USUARIO);

        assertEquals("CP-000007", comprobante.getNumeroComprobante());
        assertEquals("Juan Pérez", comprobante.getNombreUsuario());
        assertEquals("juan@bikecare.com", comprobante.getCorreoUsuario());
        assertEquals("Tarjeta de crédito/débito", comprobante.getTipoMetodo());
        assertTrue(comprobante.getDetalle().contains("VISA"));
        assertTrue(comprobante.getDetalle().contains("4242"));
        assertEquals("Activo", comprobante.getEstado());
        assertEquals("Registro de método de pago", comprobante.getConcepto());
    }

    @Test
    public void testObtenerBilleteraMuestraNumeroDeOperacion() {
        MetodoPago metodo = metodoBilletera();
        when(metodoPagoRepository.buscarDeUsuario(ID_METODO, ID_USUARIO)).thenReturn(Optional.of(metodo));

        ComprobantePago comprobante = comprobanteService.obtener(ID_METODO, ID_USUARIO);

        assertEquals("Plin", comprobante.getTipoMetodo());
        assertEquals("Operación N° ABC12345", comprobante.getDetalle());
        assertEquals("Pendiente de verificación", comprobante.getEstado());
    }

    @Test
    public void testRechazaMetodoDeOtroUsuario() {
        when(metodoPagoRepository.buscarDeUsuario(ID_METODO, ID_USUARIO)).thenReturn(Optional.empty());

        ResourceNotFoundException error = assertThrows(ResourceNotFoundException.class, () ->
                comprobanteService.obtener(ID_METODO, ID_USUARIO));

        assertTrue(error.getMessage().contains("No se encontró"));
    }

    @Test
    public void testRechazaMetodoInexistente() {
        when(metodoPagoRepository.buscarDeUsuario(999, ID_USUARIO)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                comprobanteService.obtener(999, ID_USUARIO));
    }

    // ---------- Utilidades ----------

    private MetodoPago metodoTarjeta() {
        MetodoPago metodo = new MetodoPago();
        metodo.setIdMetodoPago(ID_METODO);
        metodo.setTipo(TipoMetodoPago.TARJETA);
        metodo.setMarcaTarjeta("VISA");
        metodo.setUltimosDigitos("4242");
        metodo.setMesExpiracion(12);
        metodo.setAnioExpiracion(2030);
        metodo.setEstado(EstadoMetodoPago.ACTIVO);
        Usuario usuario = new Usuario();
        usuario.setNombre("Juan");
        usuario.setApellidos("Pérez");
        usuario.setCorreo("juan@bikecare.com");
        metodo.setUsuario(usuario);
        return metodo;
    }

    private MetodoPago metodoBilletera() {
        MetodoPago metodo = new MetodoPago();
        metodo.setIdMetodoPago(ID_METODO);
        metodo.setTipo(TipoMetodoPago.PLIN);
        metodo.setNumeroOperacion("ABC12345");
        metodo.setEstado(EstadoMetodoPago.PENDIENTE_VERIFICACION);
        Usuario usuario = new Usuario();
        usuario.setNombre("Ana");
        usuario.setApellidos("López");
        usuario.setCorreo("ana@bikecare.com");
        metodo.setUsuario(usuario);
        return metodo;
    }
}
