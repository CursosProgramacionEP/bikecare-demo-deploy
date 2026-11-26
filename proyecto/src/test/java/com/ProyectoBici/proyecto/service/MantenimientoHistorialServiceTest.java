package com.ProyectoBici.proyecto.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ProyectoBici.proyecto.entity.Mantenimiento;
import com.ProyectoBici.proyecto.repository.BicicletaRepository;
import com.ProyectoBici.proyecto.repository.MantenimientoRepository;

@ExtendWith(MockitoExtension.class)
public class MantenimientoHistorialServiceTest {

    @Mock
    private MantenimientoRepository mantenimientoRepository;

    @Mock
    private BicicletaRepository bicicletaRepository;

    @InjectMocks
    private MantenimientoHistorialService mantenimientoHistorialService;

    @Test
    public void testObtenerCostoAcumulado() {
        int idUsuario = 1;
        BigDecimal costoEsperado = new BigDecimal("150.00");

        // Simulamos que el repositorio devuelve 150.00 para este usuario
        when(mantenimientoRepository.sumarCostosAcumuladosPorUsuario(idUsuario)).thenReturn(costoEsperado);

        BigDecimal resultado = mantenimientoHistorialService.obtenerCostoAcumulado(idUsuario);

        assertNotNull(resultado);
        assertEquals(costoEsperado, resultado);
    }

    @Test
    public void testGenerarReporteCSV() {
        int idUsuario = 1;
        
        Mantenimiento m = new Mantenimiento();
        m.setIdMantenimiento(1);
        m.setDescripcion("Cambio de cadenas");
        m.setCosto(new BigDecimal("50.00"));

        when(mantenimientoRepository.listarPorUsuario(idUsuario)).thenReturn(Arrays.asList(m));
        when(mantenimientoRepository.sumarCostosAcumuladosPorUsuario(idUsuario)).thenReturn(new BigDecimal("50.00"));

        String csv = mantenimientoHistorialService.generarReporteCSV(idUsuario);

        assertNotNull(csv);
        assertTrue(csv.contains("ID,Bicicleta,Descripcion,Fecha,Costo (S/)"));
        assertTrue(csv.contains("Cambio de cadenas"));
        assertTrue(csv.contains("TOTAL ACUMULADO"));
    }
}
