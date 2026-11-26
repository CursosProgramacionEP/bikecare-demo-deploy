package com.ProyectoBici.proyecto.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.BicicletaComponente;
import com.ProyectoBici.proyecto.entity.Mantenimiento;
import com.ProyectoBici.proyecto.repository.BicicletaComponenteRepository;
import com.ProyectoBici.proyecto.repository.BicicletaRepository;
import com.ProyectoBici.proyecto.repository.MantenimientoRepository;

/**
 * Reglas de negocio del registro de mantenimientos: valida los datos del
 * formulario, comprueba que la bicicleta pertenece al usuario y guarda el
 * mantenimiento en la base de datos.
 */
@Service
public class RegistroMantenimientoService {

    private static final BigDecimal MAX_KM = new BigDecimal("99999999.99");
    private static final BigDecimal MAX_COSTO = new BigDecimal("999999.99");
    private static final int MAX_TIPO = 80;
    private static final int MAX_TALLER = 100;
    private static final String TIPO_CAMBIO_COMPONENTE = "Cambio de componente";

    @Autowired
    private BicicletaRepository bicicletaRepository;

    @Autowired
    private MantenimientoRepository mantenimientoRepository;

    @Autowired
    private BicicletaComponenteRepository bicicletaComponenteRepository;

    /**
     * Lista las bicicletas del usuario para el selector del formulario.
     *
     * @param idUsuario identificador del usuario autenticado.
     * @return bicicletas registradas por ese usuario.
     */
    @Transactional(readOnly = true)
    public List<Bicicleta> listarBicisDeUsuario(int idUsuario) {
        return bicicletaRepository.listarDeUsuario(idUsuario);
    }

    /**
     * Lista los mantenimientos del usuario, del más reciente al más antiguo.
     *
     * @param idUsuario identificador del usuario autenticado.
     * @return mantenimientos de todas sus bicicletas.
     */
    @Transactional(readOnly = true)
    public List<Mantenimiento> listarMantenimientos(int idUsuario) {
        return mantenimientoRepository.listarPorUsuario(idUsuario);
    }

    /**
     * Lista los ultimos mantenimientos del usuario, del mas reciente al mas
     * antiguo. El limite se aplica en la consulta.
     *
     * @param idUsuario identificador del usuario autenticado.
     * @param limite    cantidad maxima de registros a devolver.
     * @return mantenimientos mas recientes del usuario.
     */
    @Transactional(readOnly = true)
    public List<Mantenimiento> listarUltimosMantenimientos(int idUsuario, int limite) {
        if (limite <= 0) {
            return List.of();
        }
        return mantenimientoRepository.listarUltimosPorUsuario(idUsuario, PageRequest.of(0, limite));
    }

    /**
     * Lista los componentes activos instalados en una bicicleta.
     *
     * @param idBicicleta identificador de la bicicleta.
     * @return componentes activos con su componente de catálogo cargado.
     */
    @Transactional(readOnly = true)
    public List<BicicletaComponente> listarComponentesActivos(int idBicicleta) {
        return bicicletaComponenteRepository.listarActivosDeBicicleta(idBicicleta);
    }

    /**
     * Registra un mantenimiento sin componentes asociados.
     *
     * @param idUsuario      identificador del usuario autenticado.
     * @param idBici         bicicleta seleccionada en el formulario.
     * @param fechaRaw       fecha del servicio en formato ISO (yyyy-MM-dd).
     * @param tipoServicio   tipo de mantenimiento realizado.
     * @param kmRaw          kilometraje del odómetro al momento del servicio.
     * @param costoRaw       costo del servicio (opcional, vacío equivale a cero).
     * @param tallerMecanico taller donde se realizó el servicio (opcional).
     * @param descripcion    notas del servicio (opcional).
     * @return el mantenimiento guardado.
     * @throws IllegalArgumentException si algún dato no cumple las reglas de
     *                                  negocio.
     */
    @Transactional
    public Mantenimiento registrar(int idUsuario, Integer idBici, String fechaRaw, String tipoServicio,
            String kmRaw, String costoRaw, String tallerMecanico, String descripcion) {
        return registrar(idUsuario, idBici, fechaRaw, tipoServicio, kmRaw, costoRaw, tallerMecanico, descripcion,
                List.of());
    }

    /**
     * Registra un mantenimiento para una bicicleta del usuario. Si el tipo es
     * "Cambio de componente", reinicia a cero los kilómetros acumulados por los
     * componentes seleccionados.
     *
     * @param idUsuario      identificador del usuario autenticado.
     * @param idBici         bicicleta seleccionada en el formulario.
     * @param fechaRaw       fecha del servicio en formato ISO (yyyy-MM-dd).
     * @param tipoServicio   tipo de mantenimiento realizado.
     * @param kmRaw          kilometraje del odómetro al momento del servicio.
     * @param costoRaw       costo del servicio (opcional, vacío equivale a cero).
     * @param tallerMecanico taller donde se realizó el servicio (opcional).
     * @param descripcion    notas del servicio (opcional).
     * @param idsComponentes componentes reemplazados (solo para "Cambio de
     *                       componente").
     * @return el mantenimiento guardado.
     * @throws IllegalArgumentException si algún dato no cumple las reglas de
     *                                  negocio.
     */
    @Transactional
    public Mantenimiento registrar(int idUsuario, Integer idBici, String fechaRaw, String tipoServicio,
            String kmRaw, String costoRaw, String tallerMecanico, String descripcion,
            List<Integer> idsComponentes) {

        if (idBici == null) {
            throw new IllegalArgumentException("Selecciona una bicicleta.");
        }
        Bicicleta bici = bicicletaRepository.buscarDeUsuario(idBici, idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró esa bicicleta."));

        LocalDate fecha = parseFecha(fechaRaw);
        String tipo = parseTipo(tipoServicio);
        BigDecimal km = parseKm(kmRaw);
        BigDecimal costo = parseCosto(costoRaw);
        String taller = parseTaller(tallerMecanico);
        String notas = parseNotas(descripcion);

        List<BicicletaComponente> componentes = esCambioComponente(tipo)
                ? resolverComponentes(bici, idsComponentes)
                : List.of();

        Mantenimiento mantenimiento = new Mantenimiento();
        mantenimiento.setBicicleta(bici);
        mantenimiento.setFechaServicio(fecha);
        mantenimiento.setTipoServicio(tipo);
        mantenimiento.setKilometrajeServicio(km);
        mantenimiento.setCosto(costo);
        mantenimiento.setTallerMecanico(taller);
        mantenimiento.setDescripcion(notas);

        Mantenimiento guardado = mantenimientoRepository.save(mantenimiento);
        reiniciarKilometraje(componentes);
        return guardado;
    }

    private boolean esCambioComponente(String tipo) {
        return TIPO_CAMBIO_COMPONENTE.equalsIgnoreCase(tipo);
    }

    /**
     * Valida que los componentes seleccionados pertenezcan a la bicicleta y
     * sigan activos.
     */
    private List<BicicletaComponente> resolverComponentes(Bicicleta bici, List<Integer> idsComponentes) {
        if (idsComponentes == null || idsComponentes.isEmpty()) {
            throw new IllegalArgumentException("Selecciona al menos un componente para el cambio.");
        }

        List<BicicletaComponente> activos = bicicletaComponenteRepository
                .listarActivosDeBicicleta(bici.getIdBicicleta());
        List<BicicletaComponente> seleccionados = new ArrayList<>();

        for (Integer id : idsComponentes) {
            if (id == null) {
                continue;
            }
            BicicletaComponente encontrado = null;
            for (BicicletaComponente bc : activos) {
                if (bc.getIdBicicletaComponente() == id) {
                    encontrado = bc;
                    break;
                }
            }
            if (encontrado == null) {
                throw new IllegalArgumentException(
                        "Uno de los componentes seleccionados no pertenece a la bicicleta o ya no está activo.");
            }
            if (!seleccionados.contains(encontrado)) {
                seleccionados.add(encontrado);
            }
        }

        if (seleccionados.isEmpty()) {
            throw new IllegalArgumentException("Selecciona al menos un componente para el cambio.");
        }
        return seleccionados;
    }

    /**
     * Deja el kilometraje acumulado en cero: la instalación pasa a ser el
     * kilometraje actual del componente.
     */
    private void reiniciarKilometraje(List<BicicletaComponente> componentes) {
        if (componentes.isEmpty()) {
            return;
        }
        for (BicicletaComponente bc : componentes) {
            BigDecimal actual = bc.getKilometrajeActual() != null ? bc.getKilometrajeActual() : BigDecimal.ZERO;
            bc.setKilometrajeInstalacion(actual);
        }
        bicicletaComponenteRepository.saveAll(componentes);
    }

    private LocalDate parseFecha(String fechaRaw) {
        if (fechaRaw == null || fechaRaw.isBlank()) {
            throw new IllegalArgumentException("Selecciona la fecha del servicio.");
        }
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(fechaRaw.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La fecha del servicio no es válida.");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha del servicio no puede ser futura.");
        }
        return fecha;
    }

    private String parseTipo(String tipoServicio) {
        String tipo = tipoServicio == null ? "" : tipoServicio.trim();
        if (tipo.isEmpty()) {
            throw new IllegalArgumentException("Selecciona el tipo de mantenimiento.");
        }
        if (tipo.length() > MAX_TIPO) {
            throw new IllegalArgumentException(
                    "El tipo de mantenimiento no puede superar los " + MAX_TIPO + " caracteres.");
        }
        return tipo;
    }

    private BigDecimal parseKm(String kmRaw) {
        if (kmRaw == null || kmRaw.isBlank()) {
            throw new IllegalArgumentException("Ingresa el kilometraje del servicio.");
        }
        BigDecimal km;
        try {
            km = new BigDecimal(kmRaw.trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El kilometraje no es válido.");
        }
        if (km.signum() <= 0) {
            throw new IllegalArgumentException("El kilometraje debe ser mayor que 0.");
        }
        if (km.compareTo(MAX_KM) > 0) {
            throw new IllegalArgumentException("El kilometraje supera el máximo permitido.");
        }
        return km;
    }

    private BigDecimal parseCosto(String costoRaw) {
        if (costoRaw == null || costoRaw.isBlank()) {
            return BigDecimal.ZERO;
        }
        BigDecimal costo;
        try {
            costo = new BigDecimal(costoRaw.trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El costo no es válido.");
        }
        if (costo.signum() < 0) {
            throw new IllegalArgumentException("El costo no puede ser negativo.");
        }
        if (costo.compareTo(MAX_COSTO) > 0) {
            throw new IllegalArgumentException("El costo supera el máximo permitido.");
        }
        return costo;
    }

    private String parseTaller(String tallerMecanico) {
        String taller = tallerMecanico == null ? null : tallerMecanico.trim();
        if (taller == null || taller.isEmpty()) {
            return null;
        }
        if (taller.length() > MAX_TALLER) {
            throw new IllegalArgumentException("El taller no puede superar los " + MAX_TALLER + " caracteres.");
        }
        return taller;
    }

    private String parseNotas(String descripcion) {
        String notas = descripcion == null ? null : descripcion.trim();
        return (notas == null || notas.isEmpty()) ? null : notas;
    }
}
