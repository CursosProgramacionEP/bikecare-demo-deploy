package com.ProyectoBici.proyecto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.BicicletaComponente;
import com.ProyectoBici.proyecto.entity.Mantenimiento;
import com.ProyectoBici.proyecto.repository.BicicletaComponenteRepository;
import com.ProyectoBici.proyecto.repository.BicicletaRepository;
import com.ProyectoBici.proyecto.repository.MantenimientoRepository;

/**
 * Pruebas unitarias de {@link RegistroMantenimientoService}: validan las reglas
 * de negocio y comprueban qué datos llegan realmente al repositorio, sin usar
 * base de datos ni contexto de Spring.
 */
@ExtendWith(MockitoExtension.class)
public class RegistroMantenimientoServiceTest {

    private static final int ID_USUARIO = 1;
    private static final int ID_BICI = 10;
    private static final String FECHA_PASADA = "2026-10-20";

    @Mock
    private BicicletaRepository bicicletaRepository;

    @Mock
    private MantenimientoRepository mantenimientoRepository;

    @Mock
    private BicicletaComponenteRepository bicicletaComponenteRepository;

    @InjectMocks
    private RegistroMantenimientoService registroMantenimientoService;

    // ---------- Casos válidos ----------

    @Test
    public void testRegistrarValidoGuardaTodosLosCampos() {
        biciDelUsuario();
        guardarDevuelveElMismoObjeto();

        Mantenimiento resultado = registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                FECHA_PASADA, "Preventivo", "95.5", "45.50", "Taller Central", "Cambio de cadena");

        Mantenimiento guardado = capturarGuardado();
        assertSame(resultado, guardado);
        assertEquals(ID_BICI, guardado.getBicicleta().getIdBicicleta());
        assertEquals(LocalDate.of(2026, 10, 20), guardado.getFechaServicio());
        assertEquals("Preventivo", guardado.getTipoServicio());
        assertEquals(new BigDecimal("95.50"), guardado.getKilometrajeServicio());
        assertEquals(new BigDecimal("45.50"), guardado.getCosto());
        assertEquals("Taller Central", guardado.getTallerMecanico());
        assertEquals("Cambio de cadena", guardado.getDescripcion());
        assertNull(guardado.getComponente());
    }

    @Test
    public void testCostoVacioSeGuardaComoCero() {
        biciDelUsuario();
        guardarDevuelveElMismoObjeto();

        registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                FECHA_PASADA, "Preventivo", "95", "", null, null);

        Mantenimiento guardado = capturarGuardado();
        assertEquals(0, guardado.getCosto().compareTo(BigDecimal.ZERO));
    }

    @Test
    public void testCostoNuloSeGuardaComoCero() {
        biciDelUsuario();
        guardarDevuelveElMismoObjeto();

        registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                FECHA_PASADA, "Preventivo", "95", null, null, null);

        Mantenimiento guardado = capturarGuardado();
        assertEquals(0, guardado.getCosto().compareTo(BigDecimal.ZERO));
    }

    @Test
    public void testCamposOpcionalesVaciosSeGuardanComoNulo() {
        biciDelUsuario();
        guardarDevuelveElMismoObjeto();

        registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                FECHA_PASADA, "Preventivo", "95", "20", "   ", "   ");

        Mantenimiento guardado = capturarGuardado();
        assertNull(guardado.getTallerMecanico());
        assertNull(guardado.getDescripcion());
    }

    @Test
    public void testTipoConEspaciosSeRecorta() {
        biciDelUsuario();
        guardarDevuelveElMismoObjeto();

        registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                FECHA_PASADA, "  Correctivo  ", "95", "20", null, null);

        assertEquals("Correctivo", capturarGuardado().getTipoServicio());
    }

    @Test
    public void testKilometrajeConUnDecimalSeEscalaADosDecimales() {
        biciDelUsuario();
        guardarDevuelveElMismoObjeto();

        registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                FECHA_PASADA, "Ajuste", "95.5", null, null, null);

        assertEquals(new BigDecimal("95.50"), capturarGuardado().getKilometrajeServicio());
    }

    // ---------- Bicicleta ----------

    @Test
    public void testRechazaBicicletaNoSeleccionada() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                registroMantenimientoService.registrar(ID_USUARIO, null,
                        FECHA_PASADA, "Preventivo", "95", null, null, null));

        assertTrue(error.getMessage().contains("Selecciona una bicicleta"));
        verifyNoInteractions(mantenimientoRepository);
    }

    @Test
    public void testRechazaBicicletaDeOtroUsuario() {
        when(bicicletaRepository.buscarDeUsuario(ID_BICI, ID_USUARIO)).thenReturn(Optional.empty());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                        FECHA_PASADA, "Preventivo", "95", null, null, null));

        assertTrue(error.getMessage().contains("No se encontr"));
        verifyNoInteractions(mantenimientoRepository);
    }

    // ---------- Fecha ----------

    @Test
    public void testRechazaFechaVacia() {
        biciDelUsuario();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                        "  ", "Preventivo", "95", null, null, null));

        assertTrue(error.getMessage().contains("Selecciona la fecha"));
        verifyNoInteractions(mantenimientoRepository);
    }

    @Test
    public void testRechazaFechaFutura() {
        biciDelUsuario();
        String manana = LocalDate.now().plusDays(1).toString();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                        manana, "Preventivo", "95", null, null, null));

        assertTrue(error.getMessage().contains("no puede ser futura"));
        verifyNoInteractions(mantenimientoRepository);
    }

    @Test
    public void testRechazaFechaConFormatoInvalido() {
        biciDelUsuario();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                        "20/10/2026", "Preventivo", "95", null, null, null));

        assertTrue(error.getMessage().contains("no es válida"));
        verifyNoInteractions(mantenimientoRepository);
    }

    // ---------- Tipo de servicio ----------

    @Test
    public void testRechazaTipoVacio() {
        biciDelUsuario();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                        FECHA_PASADA, "   ", "95", null, null, null));

        assertTrue(error.getMessage().contains("tipo de mantenimiento"));
        verifyNoInteractions(mantenimientoRepository);
    }

    @Test
    public void testRechazaTipoDemasiadoLargo() {
        biciDelUsuario();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                        FECHA_PASADA, "x".repeat(81), "95", null, null, null));

        assertTrue(error.getMessage().contains("80"));
        verifyNoInteractions(mantenimientoRepository);
    }

    // ---------- Kilometraje ----------

    @Test
    public void testRechazaKilometrajeVacio() {
        biciDelUsuario();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                        FECHA_PASADA, "Preventivo", "", null, null, null));

        assertTrue(error.getMessage().contains("Ingresa el kilometraje"));
        verifyNoInteractions(mantenimientoRepository);
    }

    @Test
    public void testRechazaKilometrajeCero() {
        biciDelUsuario();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                        FECHA_PASADA, "Preventivo", "0", null, null, null));

        assertTrue(error.getMessage().contains("mayor que 0"));
        verifyNoInteractions(mantenimientoRepository);
    }

    @Test
    public void testRechazaKilometrajeNegativo() {
        biciDelUsuario();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                        FECHA_PASADA, "Preventivo", "-5", null, null, null));

        assertTrue(error.getMessage().contains("mayor que 0"));
        verifyNoInteractions(mantenimientoRepository);
    }

    @Test
    public void testRechazaKilometrajeFueraDeRango() {
        biciDelUsuario();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                        FECHA_PASADA, "Preventivo", "100000000", null, null, null));

        assertTrue(error.getMessage().contains("máximo permitido"));
        verifyNoInteractions(mantenimientoRepository);
    }

    // ---------- Costo ----------

    @Test
    public void testRechazaCostoNegativo() {
        biciDelUsuario();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                        FECHA_PASADA, "Preventivo", "95", "-1", null, null));

        assertTrue(error.getMessage().contains("no puede ser negativo"));
        verifyNoInteractions(mantenimientoRepository);
    }

    @Test
    public void testRechazaCostoFueraDeRango() {
        biciDelUsuario();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                        FECHA_PASADA, "Preventivo", "95", "1000000", null, null));

        assertTrue(error.getMessage().contains("máximo permitido"));
        verifyNoInteractions(mantenimientoRepository);
    }

    @Test
    public void testRechazaCostoNoNumerico() {
        biciDelUsuario();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                        FECHA_PASADA, "Preventivo", "95", "abc", null, null));

        assertTrue(error.getMessage().contains("no es válido"));
        verifyNoInteractions(mantenimientoRepository);
    }

    // ---------- Taller ----------

    @Test
    public void testRechazaTallerDemasiadoLargo() {
        biciDelUsuario();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                        FECHA_PASADA, "Preventivo", "95", null, "x".repeat(101), null));

        assertTrue(error.getMessage().contains("100"));
        verifyNoInteractions(mantenimientoRepository);
    }

    // ---------- Cambio de componente ----------

    @Test
    public void testCambioDeComponenteReiniciaKilometrajeAcumulado() {
        biciDelUsuario();
        guardarDevuelveElMismoObjeto();
        BicicletaComponente cadena = componente(7, "500.00", "200.00");
        BicicletaComponente pastillas = componente(8, "500.00", "0.00");
        when(bicicletaComponenteRepository.listarActivosDeBicicleta(ID_BICI))
                .thenReturn(List.of(cadena, pastillas));

        Mantenimiento guardado = registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                FECHA_PASADA, "Cambio de componente", "500", null, null, null, List.of(7, 8));

        assertEquals("Cambio de componente", guardado.getTipoServicio());
        assertEquals(new BigDecimal("500.00"), cadena.getKilometrajeInstalacion());
        assertEquals(new BigDecimal("500.00"), pastillas.getKilometrajeInstalacion());
        assertEquals(0, cadena.getKilometrajeAcumulado().compareTo(BigDecimal.ZERO));
        assertEquals(0, pastillas.getKilometrajeAcumulado().compareTo(BigDecimal.ZERO));
        verify(bicicletaComponenteRepository).saveAll(List.of(cadena, pastillas));
    }

    @Test
    public void testCambioDeComponenteExigeSeleccion() {
        biciDelUsuario();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                        FECHA_PASADA, "Cambio de componente", "500", null, null, null, List.of()));

        assertTrue(error.getMessage().contains("al menos un componente"));
        verifyNoInteractions(mantenimientoRepository);
        verifyNoInteractions(bicicletaComponenteRepository);
    }

    @Test
    public void testCambioDeComponenteRechazaComponenteAjeno() {
        biciDelUsuario();
        when(bicicletaComponenteRepository.listarActivosDeBicicleta(ID_BICI)).thenReturn(List.of());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                        FECHA_PASADA, "Cambio de componente", "500", null, null, null, List.of(99)));

        assertTrue(error.getMessage().contains("no pertenece"));
        verifyNoInteractions(mantenimientoRepository);
    }

    @Test
    public void testTipoNormalNoTocaLosComponentes() {
        biciDelUsuario();
        guardarDevuelveElMismoObjeto();

        registroMantenimientoService.registrar(ID_USUARIO, ID_BICI,
                FECHA_PASADA, "Preventivo", "500", null, null, null, List.of(7));

        verifyNoInteractions(bicicletaComponenteRepository);
    }

    // ---------- Consultas ----------

    @Test
    public void testListarBicisDeUsuarioDelegaEnRepositorio() {
        List<Bicicleta> bicis = List.of(bici());
        when(bicicletaRepository.listarDeUsuario(ID_USUARIO)).thenReturn(bicis);

        assertEquals(bicis, registroMantenimientoService.listarBicisDeUsuario(ID_USUARIO));
    }

    @Test
    public void testListarMantenimientosDelegaEnRepositorio() {
        List<Mantenimiento> mantenimientos = List.of(new Mantenimiento());
        when(mantenimientoRepository.listarPorUsuario(ID_USUARIO)).thenReturn(mantenimientos);

        assertEquals(mantenimientos, registroMantenimientoService.listarMantenimientos(ID_USUARIO));
    }

    @Test
    public void testListarUltimosMantenimientosLimitaLaConsulta() {
        List<Mantenimiento> mantenimientos = List.of(new Mantenimiento());
        when(mantenimientoRepository.listarUltimosPorUsuario(eq(ID_USUARIO), any(Pageable.class)))
                .thenReturn(mantenimientos);

        assertEquals(mantenimientos, registroMantenimientoService.listarUltimosMantenimientos(ID_USUARIO, 2));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(mantenimientoRepository).listarUltimosPorUsuario(eq(ID_USUARIO), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(2, captor.getValue().getPageSize());
    }

    @Test
    public void testListarUltimosMantenimientosConLimiteInvalidoNoConsulta() {
        assertTrue(registroMantenimientoService.listarUltimosMantenimientos(ID_USUARIO, 0).isEmpty());
        verifyNoInteractions(mantenimientoRepository);
    }

    // ---------- Utilidades ----------

    private Bicicleta bici() {
        Bicicleta bicicleta = new Bicicleta();
        bicicleta.setIdBicicleta(ID_BICI);
        bicicleta.setMarca("Trek");
        bicicleta.setModelo("Marlin 5");
        bicicleta.setKilometrajeTotal(new BigDecimal("100.00"));
        return bicicleta;
    }

    private BicicletaComponente componente(int id, String actual, String instalacion) {
        BicicletaComponente bc = new BicicletaComponente();
        bc.setIdBicicletaComponente(id);
        bc.setKilometrajeActual(new BigDecimal(actual));
        bc.setKilometrajeInstalacion(new BigDecimal(instalacion));
        return bc;
    }

    private void biciDelUsuario() {
        when(bicicletaRepository.buscarDeUsuario(ID_BICI, ID_USUARIO)).thenReturn(Optional.of(bici()));
    }

    private void guardarDevuelveElMismoObjeto() {
        when(mantenimientoRepository.save(any(Mantenimiento.class)))
                .thenAnswer(inv -> inv.getArgument(0, Mantenimiento.class));
    }

    private Mantenimiento capturarGuardado() {
        ArgumentCaptor<Mantenimiento> captor = ArgumentCaptor.forClass(Mantenimiento.class);
        verify(mantenimientoRepository).save(captor.capture());
        return captor.getValue();
    }
}
