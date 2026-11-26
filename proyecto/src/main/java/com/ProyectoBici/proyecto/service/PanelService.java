package com.ProyectoBici.proyecto.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ProyectoBici.proyecto.dto.ComponenteEstado;
import com.ProyectoBici.proyecto.dto.PanelResumen;
import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.repository.BicicletaRepository;

//HU11: Consultar historial de mantenimientos pasados
@Service
public class PanelService {

    @Autowired
    private BicicletaRepository bicicletaRepository;

    @Autowired
    private DesgasteComponenteService desgasteComponenteService;

    /**
     * Arma todos los datos del panel para un usuario y una bici opcional.
     * Si biciId es null o ajena, usa la primera de la lista (o null si vacío).
     */
    @Transactional(readOnly = true)
    public PanelResumen armarPanel(int idUsuario, Integer biciId) {
        // Todas las bicis del usuario (la más nueva primero).
        List<Bicicleta> bicis = bicicletaRepository.listarDeUsuario(idUsuario);

        // Suma de odómetros.
        BigDecimal kmTotales = BigDecimal.ZERO;
        for (Bicicleta b : bicis) {
            if (b.getKilometrajeTotal() != null) {
                kmTotales = kmTotales.add(b.getKilometrajeTotal());
            }
        }

        // Alertas globales: componentes en nivel alerta/crítico (80% revisar, 100% reemplazar).
        int totalAlertas = 0;
        for (Bicicleta b : bicis) {
            for (ComponenteEstado e : desgasteComponenteService.estadoDeComponentes(b.getIdBicicleta())) {
                if (!"ok".equals(e.getNivel())) {
                    totalAlertas++;
                }
            }
        }

        // Bici del desplegable: la pedida si es propia, si no la primera.
        Bicicleta seleccionada = null;
        if (biciId != null) {
            seleccionada = bicicletaRepository.buscarDeUsuario(biciId, idUsuario).orElse(null);
        }
        if (seleccionada == null && !bicis.isEmpty()) {
            seleccionada = bicis.get(0);
        }

        // Detalle de la seleccionada + solo sus alertas.
        List<ComponenteEstado> componentesSel = new ArrayList<>();
        List<ComponenteEstado> alertasSel = new ArrayList<>();
        if (seleccionada != null) {
            componentesSel = desgasteComponenteService.estadoDeComponentes(seleccionada.getIdBicicleta());
            for (ComponenteEstado e : componentesSel) {
                if (!"ok".equals(e.getNivel())) {
                    alertasSel.add(e);
                }
            }
        }

        return new PanelResumen(bicis, kmTotales, totalAlertas,
                seleccionada, componentesSel, alertasSel);
    }
}
