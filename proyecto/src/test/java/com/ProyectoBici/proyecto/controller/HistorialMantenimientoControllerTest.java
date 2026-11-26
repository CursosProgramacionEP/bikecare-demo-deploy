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

import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.service.MantenimientoHistorialService;

@ExtendWith(MockitoExtension.class)
public class HistorialMantenimientoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MantenimientoHistorialService mantenimientoHistorialService;

    @InjectMocks
    private HistorialMantenimientoController historialMantenimientoController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(historialMantenimientoController).build();
    }

    @Test
    public void testExportarReporteCSVEndpoint() throws Exception {
        int idUsuarioPrueba = 1;
        
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId_usuario(idUsuarioPrueba);

        String csvSimulado = "ID,Bicicleta,Descripcion,Fecha,Costo (S/)\n1,Trek,Cambio,2026-10-19,50.00";
        byte[] bytesCsv = csvSimulado.getBytes();

        when(mantenimientoHistorialService.generarReporteCSV(idUsuarioPrueba)).thenReturn(csvSimulado);

        // Simulamos la petición GET enviando el objeto de sesión y validando el nombre correcto del archivo
        mockMvc.perform(get("/welcome/historial/exportar")
                .sessionAttr("usuarioLogueado", usuarioMock))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment; filename=reporte_mantenimientos_bikecare.csv")))
                .andExpect(content().bytes(bytesCsv));
    }
}