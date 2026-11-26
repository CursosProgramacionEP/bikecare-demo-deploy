package com.ProyectoBici.proyecto.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ProyectoBici.proyecto.dto.ComponenteEstado;
import com.ProyectoBici.proyecto.entity.BicicletaComponente;
import com.ProyectoBici.proyecto.entity.Umbral;
import com.ProyectoBici.proyecto.repository.BicicletaComponenteRepository;
import com.ProyectoBici.proyecto.repository.UmbralRepository;


@Service
public class DesgasteComponenteService {

    private static final int PORCENTAJE_ALERTA_POR_DEFECTO = 80;

    @Autowired
    private BicicletaComponenteRepository bicicletaComponenteRepository;

    @Autowired
    private UmbralRepository umbralRepository;

    // Estado de desgaste (por km) de cada componente activo de la bici.
    @Transactional(readOnly = true)
    public List<ComponenteEstado> estadoDeComponentes(int idBicicleta) {
        List<ComponenteEstado> resultado = new ArrayList<>();
        for (BicicletaComponente bc : bicicletaComponenteRepository.listarActivosDeBicicleta(idBicicleta)) {
            BigDecimal actual = bc.getKilometrajeActual() != null ? bc.getKilometrajeActual() : BigDecimal.ZERO;
            BigDecimal instalacion = bc.getKilometrajeInstalacion() != null ? bc.getKilometrajeInstalacion()
                    : BigDecimal.ZERO;
            BigDecimal usados = actual.subtract(instalacion).max(BigDecimal.ZERO);
            int kmUsados = usados.setScale(0, RoundingMode.HALF_UP).intValue();

            List<Umbral> umbrales = umbralRepository.listarActivosDeComponente(bc.getIdBicicletaComponente());
            Umbral umbral = umbrales.isEmpty() ? null : umbrales.get(0);

            int limite = 0;
            int porcentaje = 0;
            int porcentajeAlerta = PORCENTAJE_ALERTA_POR_DEFECTO;
            if (umbral != null && umbral.getLimiteKm() != null && umbral.getLimiteKm().signum() > 0) {
                limite = umbral.getLimiteKm().setScale(0, RoundingMode.HALF_UP).intValue();
                porcentaje = (int) Math.round(usados.doubleValue() * 100.0 / umbral.getLimiteKm().doubleValue());
                if (umbral.getPorcentajeAlerta() != null) {
                    porcentajeAlerta = umbral.getPorcentajeAlerta();
                }
            }

            String nivel;
            String etiqueta;
            if (limite > 0 && porcentaje >= 100) {
                nivel = "critico";
                etiqueta = "Reemplazar";
            } else if (limite > 0 && porcentaje >= porcentajeAlerta) {
                nivel = "alerta";
                etiqueta = "Revisar";
            } else {
                nivel = "ok";
                etiqueta = "Buen estado";
            }

            resultado.add(new ComponenteEstado(
                    bc.getComponente().getNombre(),
                    bc.getComponente().getIcono() != null ? bc.getComponente().getIcono() : "🔧",
                    kmUsados, limite, porcentaje, Math.min(100, porcentaje), nivel, etiqueta,
                    // Ids para que el rol Taller pueda editar el límite desde el Garaje.
                    bc.getIdBicicletaComponente(),
                    umbral != null ? umbral.getIdUmbral() : null));
        }
        return resultado;
    }
}
