package com.ProyectoBici.proyecto.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ProyectoBici.proyecto.dto.ComprobantePago;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.service.ComprobantePagoService;

@ExtendWith(MockitoExtension.class)
public class ComprobantePagoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ComprobantePagoService comprobanteService;

    @InjectMocks
    private ComprobantePagoController comprobanteController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(comprobanteController).build();
    }

    @Test
    public void testVerComprobanteConSesionMuestraLaVista() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId_usuario(1);
        usuario.setNombre("Juan");
        usuario.setApellidos("Pérez");

        ComprobantePago comprobante = new ComprobantePago();
        comprobante.setIdComprobante(7);
        comprobante.setNumeroComprobante("CP-000007");
        comprobante.setNombreUsuario("Juan Pérez");
        comprobante.setTipoMetodo("Tarjeta de crédito/débito");
        comprobante.setDetalle("VISA **** 4242");
        comprobante.setEstado("Activo");
        comprobante.setConcepto("Registro de método de pago");

        when(comprobanteService.obtener(7, 1)).thenReturn(comprobante);

        mockMvc.perform(get("/welcome/pagos/comprobante/7")
                .sessionAttr("usuarioLogueado", usuario))
                .andExpect(status().isOk())
                .andExpect(view().name("comprobante-pago"))
                .andExpect(model().attributeExists("comprobante", "usuario"))
                .andExpect(model().attribute("comprobante", comprobante));
    }

    @Test
    public void testVerComprobanteMetodoAjenoLanzaExcepcion() {
        Usuario usuario = new Usuario();
        usuario.setId_usuario(1);

        when(comprobanteService.obtener(7, 1))
                .thenThrow(new com.ProyectoBici.proyecto.shared.exception.ResourceNotFoundException(
                        "No se encontró ese método de pago.", "/welcome/pagos"));

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                mockMvc.perform(get("/welcome/pagos/comprobante/7")
                        .sessionAttr("usuarioLogueado", usuario)));
    }

    @Test
    public void testVerComprobanteSinSesionLanzaExcepcion() {
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                mockMvc.perform(get("/welcome/pagos/comprobante/7")));
    }
}
