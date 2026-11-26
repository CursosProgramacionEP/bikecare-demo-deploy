package com.ProyectoBici.proyecto.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.BicicletaComponente;
import com.ProyectoBici.proyecto.entity.Umbral;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.repository.BicicletaComponenteRepository;
import com.ProyectoBici.proyecto.repository.UmbralRepository;

@ExtendWith(MockitoExtension.class)
public class UmbralServiceTest {

    @Mock
    private BicicletaComponenteRepository bicicletaComponenteRepository;

    @Mock
    private UmbralRepository umbralRepository;

    @InjectMocks
    private UmbralService umbralService;

    @Test
    public void testActualizarLimiteKmExitoso() {
        int idUsuario = 1;
        int idBc = 10;
        BigDecimal nuevoLimite = new BigDecimal("5000.00");

        Usuario usuario = new Usuario();
        usuario.setId_usuario(idUsuario);

        Bicicleta bicicleta = new Bicicleta();
        bicicleta.setUsuario(usuario);

        BicicletaComponente bc = new BicicletaComponente();
        bc.setIdBicicletaComponente(idBc);
        bc.setBicicleta(bicicleta);

        when(bicicletaComponenteRepository.findById(idBc)).thenReturn(Optional.of(bc));
        when(umbralRepository.listarActivosDeComponente(idBc)).thenReturn(new ArrayList<>());
        when(umbralRepository.save(any(Umbral.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Umbral resultado = umbralService.actualizarLimiteKm(idUsuario, idBc, nuevoLimite);

        assertNotNull(resultado);
        assertEquals(0, nuevoLimite.compareTo(resultado.getLimiteKm()));
        assertEquals(1, resultado.getActivo());
    }

    @Test
    public void testActualizarLimiteKmFueraDeRangoExcepcion() {
        int idUsuario = 1;
        int idBc = 10;
        BigDecimal limiteInvalido = new BigDecimal("10.00"); // Menor a 50

        Usuario usuario = new Usuario();
        usuario.setId_usuario(idUsuario);

        Bicicleta bicicleta = new Bicicleta();
        bicicleta.setUsuario(usuario);

        BicicletaComponente bc = new BicicletaComponente();
        bc.setIdBicicletaComponente(idBc);
        bc.setBicicleta(bicicleta);

        when(bicicletaComponenteRepository.findById(idBc)).thenReturn(Optional.of(bc));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            umbralService.actualizarLimiteKm(idUsuario, idBc, limiteInvalido);
        });

        // Mensaje corregido con tilde y punto final idéntico al servicio
        assertTrue(exception.getMessage().contains("El límite debe estar entre 50 y 100000 km."));
    }
}