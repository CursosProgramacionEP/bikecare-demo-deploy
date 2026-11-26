package com.ProyectoBici.proyecto.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ProyectoBici.proyecto.entity.BicicletaComponente;
import com.ProyectoBici.proyecto.entity.Umbral;
import com.ProyectoBici.proyecto.repository.BicicletaComponenteRepository;
import com.ProyectoBici.proyecto.repository.UmbralRepository;

/**
 * Solo la EDICIÓN del límite por kilometraje (rol Taller).
 * Valida propiedad y rango, reutiliza o crea el umbral activo.
 */
@Service
public class UmbralService {

    private static final int PORCENTAJE_ALERTA_POR_DEFECTO = 80;

    @Autowired
    private BicicletaComponenteRepository bicicletaComponenteRepository;

    @Autowired
    private UmbralRepository umbralRepository;

    /**
     * Actualiza (o crea) el límite por kilometraje de un componente instalado.
     * Solo debe llamarlo el rol Taller (el controlador lo verifica antes).
     *
     * @param idUsuario id del usuario logueado (el componente debe ser de una de sus bicis).
     * @param idBc      id de bicicleta_componente a ajustar.
     * @param limiteKm  nuevo límite en km (validado: 50..100000).
     * @return el umbral guardado (con su componente para el mensaje de éxito).
     * @throws IllegalArgumentException si no es propio, no existe o el límite es inválido.
     */
    @Transactional
    public Umbral actualizarLimiteKm(int idUsuario, int idBc, BigDecimal limiteKm) {
        // 1) El componente debe existir y pertenecer a una bici del usuario.
        BicicletaComponente bc = bicicletaComponenteRepository.findById(idBc).orElse(null);
        if (bc == null || bc.getBicicleta() == null
                || bc.getBicicleta().getUsuario().getId_usuario() != idUsuario) {
            throw new IllegalArgumentException("No se encontró ese componente en tu garaje.");
        }
        // 2) Validar rango razonable para un límite por kilometraje.
        if (limiteKm == null) {
            throw new IllegalArgumentException("El límite en km es obligatorio.");
        }
        limiteKm = limiteKm.setScale(2, RoundingMode.HALF_UP);
        if (limiteKm.compareTo(new BigDecimal("50")) < 0
                || limiteKm.compareTo(new BigDecimal("100000")) > 0) {
            throw new IllegalArgumentException("El límite debe estar entre 50 y 100000 km.");
        }
        // 3) Reutilizar el umbral activo si existe; si no, crear uno nuevo.
        List<Umbral> activos = umbralRepository.listarActivosDeComponente(idBc);
        Umbral umbral = activos.isEmpty() ? new Umbral() : activos.get(0);
        if (umbral.getIdUmbral() == 0) {
            umbral.setBicicletaComponente(bc);
            umbral.setPorcentajeAlerta(PORCENTAJE_ALERTA_POR_DEFECTO);
            umbral.setActivo(1);
        }
        umbral.setLimiteKm(limiteKm);
        umbral.setActivo(1);
        return umbralRepository.save(umbral);
    }
}
