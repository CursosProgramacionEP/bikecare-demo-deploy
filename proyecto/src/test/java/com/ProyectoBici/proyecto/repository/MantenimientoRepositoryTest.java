package com.ProyectoBici.proyecto.repository;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MantenimientoRepositoryTest {

    @Autowired
    private MantenimientoRepository mantenimientoRepository;

    @Test
    public void testSumarCostosAcumuladosPorUsuario() {
        int idUsuarioPrueba = 1;
        
        // Ejecutamos la consulta que implementaste para la HU13
        BigDecimal costoTotal = mantenimientoRepository.sumarCostosAcumuladosPorUsuario(idUsuarioPrueba);

        // Verificamos que no sea nulo y que sea mayor o igual a cero
        assertNotNull(costoTotal);
        assertTrue(costoTotal.compareTo(BigDecimal.ZERO) >= 0);
    }
}