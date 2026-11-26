package com.ProyectoBici.proyecto.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ProyectoBici.proyecto.entity.Componente;
import com.ProyectoBici.proyecto.repository.BicicletaComponenteRepository;
import com.ProyectoBici.proyecto.repository.ComponenteRepository;

/**
 * Solo LECTURAS del catálogo e instalados (para pintar el formulario).
 * No crea ni calcula nada.
 */
@Service
public class ComponenteCatalogoService {

    @Autowired
    private ComponenteRepository componenteRepository;

    @Autowired
    private BicicletaComponenteRepository bicicletaComponenteRepository;

    // Catálogo completo (lo que se muestra como checkboxes en el formulario).
    public List<Componente> listarCatalogo() {
        return componenteRepository.findAllByOrderByIdComponenteAsc();
    }

    // Ids del catálogo que la bici ya tiene instalados.
    @Transactional(readOnly = true)
    public Set<Integer> idsInstalados(int idBicicleta) {
        return bicicletaComponenteRepository.listarActivosDeBicicleta(idBicicleta).stream()
                .map(bc -> bc.getComponente().getIdComponente())
                .collect(Collectors.toSet());
    }
}
