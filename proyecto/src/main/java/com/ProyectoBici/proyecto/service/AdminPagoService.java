package com.ProyectoBici.proyecto.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ProyectoBici.proyecto.entity.EstadoMetodoPago;
import com.ProyectoBici.proyecto.entity.MetodoPago;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.repository.MetodoPagoRepository;
import com.ProyectoBici.proyecto.repository.UsuarioRepository;

@Service
public class AdminPagoService {

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<MetodoPago> obtenerPagosPendientes() {
        return metodoPagoRepository.listarPorEstado(EstadoMetodoPago.PENDIENTE_VERIFICACION);
    }

    @Transactional
    public void aprobarPago(int idMetodoPago) {
        MetodoPago pago = metodoPagoRepository.findById(idMetodoPago)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        pago.setEstado(EstadoMetodoPago.APROBADO);
        metodoPagoRepository.save(pago);

        Usuario usuario = pago.getUsuario();
        if (usuario != null) {
            usuario.setEsPremium(true);
            usuarioRepository.save(usuario);
        }
    }
}