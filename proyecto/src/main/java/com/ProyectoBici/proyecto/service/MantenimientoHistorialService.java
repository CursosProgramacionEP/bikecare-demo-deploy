package com.ProyectoBici.proyecto.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.Mantenimiento;
import com.ProyectoBici.proyecto.repository.BicicletaRepository;
import com.ProyectoBici.proyecto.repository.MantenimientoRepository;

@Service
public class MantenimientoHistorialService {

    @Autowired
    private MantenimientoRepository mantenimientoRepository;

    @Autowired
    private BicicletaRepository bicicletaRepository;

    @Transactional(readOnly = true)
    public List<Bicicleta> listarBicicletasUsuario(int idUsuario) {
        return bicicletaRepository.listarDeUsuario(idUsuario);
    }

    @Transactional(readOnly = true)
    public List<Mantenimiento> obtenerHistorial(int idUsuario, Integer idBicicleta) {
        if (idBicicleta != null && idBicicleta > 0) {
            return mantenimientoRepository.listarPorBicicletaDeUsuario(idUsuario, idBicicleta);
        }
        return mantenimientoRepository.listarPorUsuario(idUsuario);
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerCostoAcumulado(int idUsuario) {
        BigDecimal total = mantenimientoRepository.sumarCostosAcumuladosPorUsuario(idUsuario);
        return total != null ? total : BigDecimal.ZERO;
    }

    public String generarReporteCSV(int idUsuario) {
        List<Mantenimiento> lista = mantenimientoRepository.listarPorUsuario(idUsuario);
        BigDecimal total = obtenerCostoAcumulado(idUsuario);

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Bicicleta,Descripcion,Fecha,Costo (S/)\n");

        for (Mantenimiento m : lista) {
            String nombreBici = (m.getBicicleta() != null) ? m.getBicicleta().getModelo() : "N/A";
            csv.append(m.getIdMantenimiento()).append(",")
               .append("\"").append(nombreBici).append("\",")
               .append("\"").append(m.getDescripcion() != null ? m.getDescripcion() : "Mantenimiento").append("\",")
               .append(m.getFechaServicio()).append(",")
               .append(m.getCosto()).append("\n");
        }

        csv.append("\n,,,TOTAL ACUMULADO,S/ ").append(total).append("\n");
        return csv.toString();
    }
}