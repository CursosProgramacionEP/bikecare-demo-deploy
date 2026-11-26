package com.ProyectoBici.proyecto.service;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ProyectoBici.proyecto.dto.ComponenteInstalacionForm;
import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.BicicletaComponente;
import com.ProyectoBici.proyecto.entity.Componente;
import com.ProyectoBici.proyecto.entity.Umbral;
import com.ProyectoBici.proyecto.repository.BicicletaComponenteRepository;
import com.ProyectoBici.proyecto.repository.ComponenteRepository;
import com.ProyectoBici.proyecto.repository.UmbralRepository;

/**
 * Solo la INSTALACIÓN de componentes en una bici
 * (crea bicicleta_componente + su umbral inicial).
 * Recibe formularios ya validados, nunca params crudos del form.
 */
@Service
public class ComponenteInstalacionService {

    private static final int PORCENTAJE_ALERTA_POR_DEFECTO = 80;

    @Autowired
    private ComponenteRepository componenteRepository;

    @Autowired
    private BicicletaComponenteRepository bicicletaComponenteRepository;

    @Autowired
    private UmbralRepository umbralRepository;

    /**
     * Guarda los componentes del formulario.
     *
     * @return cuántos componentes se guardaron.
     */
    @Transactional
    public int agregarComponentes(Bicicleta bici, List<ComponenteInstalacionForm> formas) {
        if (formas == null || formas.isEmpty()) {
            return 0;
        }

        Set<Integer> yaInstalados = bicicletaComponenteRepository
                .listarActivosDeBicicleta(bici.getIdBicicleta()).stream()
                .map(bc -> bc.getComponente().getIdComponente())
                .collect(Collectors.toSet());
        BigDecimal kmBici = bici.getKilometrajeTotal() != null ? bici.getKilometrajeTotal() : BigDecimal.ZERO;
        int guardados = 0;

        // LinkedHashSet: evita repetidos y respeta el orden.
        Set<Integer> vistos = new LinkedHashSet<>();
        for (ComponenteInstalacionForm forma : formas) {
            if (forma == null || !vistos.add(forma.idComponente()) || yaInstalados.contains(forma.idComponente())) {
                continue; // repetido en el form o ya instalado, no lo duplicamos
            }
            Componente comp = componenteRepository.findById(forma.idComponente()).orElse(null);
            if (comp == null) {
                continue;
            }

            BicicletaComponente bc = new BicicletaComponente();
            bc.setBicicleta(bici);
            bc.setComponente(comp);
            bc.setMarca(forma.marca());
            bc.setModelo(forma.modelo());
            bc.setFechaInstalacion(forma.fechaInstalacion());
            bc.setKilometrajeInstalacion(forma.kilometrajeInstalacion());
            bc.setKilometrajeActual(kmBici);
            bc.setEstado("activo");
            bc = bicicletaComponenteRepository.save(bc);

            // Umbral en km: al llegar al % de alerta se avisa, al 100 % toca cambiarlo.
            if (comp.getVidaUtilReferencial() != null && comp.getVidaUtilReferencial().signum() > 0) {
                Umbral umbral = new Umbral();
                umbral.setBicicletaComponente(bc);
                umbral.setLimiteKm(comp.getVidaUtilReferencial());
                umbral.setPorcentajeAlerta(PORCENTAJE_ALERTA_POR_DEFECTO);
                umbral.setActivo(1);
                umbralRepository.save(umbral);
            }
            guardados++;
        }
        return guardados;
    }
}
