package com.ProyectoBici.proyecto.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ProyectoBici.proyecto.dto.BicicletaForm;
import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.repository.BicicletaRepository;
import com.ProyectoBici.proyecto.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
public class BicicletaServiceTest {

    @Mock
    private BicicletaRepository bicicletaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private DesgasteComponenteService desgasteComponenteService;

    @InjectMocks
    private BicicletaService bicicletaService;

    @Test
    public void testRegistrarBicicletaExitoso() {
        Usuario usuarioSesion = new Usuario();
        usuarioSesion.setId_usuario(1);
        usuarioSesion.setEsPremium(false);

        BicicletaForm form = new BicicletaForm();
        form.setMarca("Trek");
        form.setModelo("Marlin 7");
        form.setTipo("Montaña");
        form.setAnio(2023);
        form.setNumeroSerie("TRK98765");
        form.setKilometrajeTotal(new BigDecimal("150.00"));

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioSesion));
        when(bicicletaRepository.listarDeUsuario(1)).thenReturn(List.of());
        when(bicicletaRepository.save(any(Bicicleta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Bicicleta resultado = bicicletaService.registrar(usuarioSesion, form);

        assertNotNull(resultado);
        assertEquals("Trek", resultado.getMarca());
        assertEquals("Marlin 7", resultado.getModelo());
        assertEquals("TRK98765", resultado.getNumeroSerie());
        assertEquals(2023, resultado.getAnio());
    }

    @Test
    public void testRegistrarBicicletaAnioInvalidoExcepcion() {
        Usuario usuarioSesion = new Usuario();
        usuarioSesion.setId_usuario(1);
        usuarioSesion.setEsPremium(false);

        BicicletaForm form = new BicicletaForm();
        form.setMarca("Trek");
        form.setModelo("Marlin 7");
        form.setTipo("Montaña");
        form.setAnio(1800); // Año inválido fuera de rango (1900 - 2100)
        form.setKilometrajeTotal(new BigDecimal("150.00"));

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioSesion));
        when(bicicletaRepository.listarDeUsuario(1)).thenReturn(List.of());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bicicletaService.registrar(usuarioSesion, form);
        });

        assertTrue(exception.getMessage().contains("El año debe estar entre 1900 y 2100."));
    }

    @Test
    public void testRechazaLimiteBicicletasPlanGratuito() {
        Usuario usuarioSesion = new Usuario();
        usuarioSesion.setId_usuario(1);
        usuarioSesion.setEsPremium(false);

        BicicletaForm form = new BicicletaForm();
        form.setMarca("Giant");
        form.setModelo("Talon 1");
        form.setTipo("Montaña");
        form.setKilometrajeTotal(new BigDecimal("50.00"));

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioSesion));
        when(bicicletaRepository.listarDeUsuario(1)).thenReturn(List.of(new Bicicleta(), new Bicicleta()));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bicicletaService.registrar(usuarioSesion, form);
        });

        assertTrue(exception.getMessage().contains("Alcanzaste el límite de 2 bicicletas"));
        verify(bicicletaRepository, never()).save(any());
    }

    @Test
    public void testRechazaModeloConCaracteresInvalidos() {
        Usuario usuarioSesion = new Usuario();
        usuarioSesion.setId_usuario(1);
        usuarioSesion.setEsPremium(true);

        BicicletaForm form = new BicicletaForm();
        form.setMarca("Trek");
        form.setModelo("@@@###");
        form.setTipo("Ruta");
        form.setKilometrajeTotal(new BigDecimal("10.00"));

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioSesion));
        when(bicicletaRepository.listarDeUsuario(1)).thenReturn(List.of());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bicicletaService.registrar(usuarioSesion, form);
        });

        assertTrue(exception.getMessage().contains("El modelo solo puede contener"));
    }

    @Test
    public void testRechazaNumeroSerieInvalido() {
        Usuario usuarioSesion = new Usuario();
        usuarioSesion.setId_usuario(1);
        usuarioSesion.setEsPremium(true);

        BicicletaForm form = new BicicletaForm();
        form.setMarca("Specialized");
        form.setModelo("Allez");
        form.setTipo("Ruta");
        form.setNumeroSerie("12"); // Muy corto y/o inválido
        form.setKilometrajeTotal(new BigDecimal("0.00"));

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioSesion));
        when(bicicletaRepository.listarDeUsuario(1)).thenReturn(List.of());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bicicletaService.registrar(usuarioSesion, form);
        });

        assertTrue(exception.getMessage().contains("El número de serie debe ser alfanumérico"));
    }
}