package com.ProyectoBici.proyecto.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.BicicletaComponente;
import com.ProyectoBici.proyecto.repository.BicicletaComponenteRepository;
import com.ProyectoBici.proyecto.repository.BicicletaRepository;

/**
 * Solo la REGLA DE NEGOCIO de registrar un recorrido.
 * Suma la distancia al odómetro de la bici y al kilometraje actual
 * de sus componentes activos. No sabe nada de HTTP/sesión/redirects.
 */
@Service
public class RutaService {

    private static final BigDecimal MIN_KM = new BigDecimal("0.10");
    private static final BigDecimal MAX_KM_RUTA = new BigDecimal("1000.00");
    private static final BigDecimal MAX_KM_ODOMETRO = new BigDecimal("99999999.99");

    @Autowired
    private BicicletaRepository bicicletaRepository;

    @Autowired
    private BicicletaComponenteRepository bicicletaComponenteRepository;

    /**
     * Lista todas las bicicletas registradas por el usuario para poblarlas en formularios de selección.
     *
     * @param idUsuario Identificador del ciclista propietario.
     * @return Lista de entidades {@link Bicicleta} pertenecientes a dicho usuario.
     */

    @Transactional(readOnly = true)
    public List<Bicicleta> listarBicisDeUsuario(int idUsuario) {
        return bicicletaRepository.listarDeUsuario(idUsuario);
    }

   /**
     * Registra un recorrido sumando los kilómetros ingresados tanto al odómetro de la bicicleta
     * como a los componentes mecánicos que se encuentren activos.
     *
     * @param idUsuario Identificador del usuario propietario que realiza la ruta.
     * @param idBici Identificador único de la bicicleta utilizada.
     * @param distancia Distancia recorrida durante la salida.
     * @return La entidad {@link Bicicleta} actualizada con su nuevo kilometraje total acumulado.
     * @throws IllegalArgumentException Si la distancia es nula, está fuera del rango [0.10, 1000.00] km,
     *                                  si la bicicleta no existe o no pertenece al usuario,
     *                                  o si el nuevo total supera el límite del odómetro.
     */
    
    @Transactional
    public Bicicleta registrarRecorrido(int idUsuario, int idBici, BigDecimal distancia) {
        if (distancia == null) {
            throw new IllegalArgumentException("La distancia no es válida.");
        }
        distancia = distancia.setScale(2, RoundingMode.HALF_UP);
        if (distancia.compareTo(MIN_KM) < 0 || distancia.compareTo(MAX_KM_RUTA) > 0) {
            throw new IllegalArgumentException("La distancia debe estar entre 0.10 y 1000.00 km.");
        }

        Bicicleta bici = bicicletaRepository.buscarDeUsuario(idBici, idUsuario).orElse(null);
        if (bici == null) {
            throw new IllegalArgumentException("No se encontró esa bicicleta.");
        }

        BigDecimal odometro = bici.getKilometrajeTotal() != null ? bici.getKilometrajeTotal() : BigDecimal.ZERO;
        BigDecimal nuevoOdometro = odometro.add(distancia).setScale(2, RoundingMode.HALF_UP);
        if (nuevoOdometro.compareTo(MAX_KM_ODOMETRO) > 0) {
            throw new IllegalArgumentException("El odómetro superaría el límite permitido.");
        }

        bici.setKilometrajeTotal(nuevoOdometro);
        bicicletaRepository.save(bici);

        List<BicicletaComponente> activos = bicicletaComponenteRepository.listarActivosDeBicicleta(bici.getIdBicicleta());
        for (BicicletaComponente bc : activos) {
            BigDecimal actual = bc.getKilometrajeActual() != null ? bc.getKilometrajeActual() : BigDecimal.ZERO;
            bc.setKilometrajeActual(actual.add(distancia).setScale(2, RoundingMode.HALF_UP));
            bicicletaComponenteRepository.save(bc);
        }
        return bici;
    }
}
