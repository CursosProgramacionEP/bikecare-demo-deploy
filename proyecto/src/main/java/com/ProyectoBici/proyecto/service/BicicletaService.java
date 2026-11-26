package com.ProyectoBici.proyecto.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ProyectoBici.proyecto.dto.BicicletaCard;
import com.ProyectoBici.proyecto.dto.BicicletaForm;
import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.repository.BicicletaRepository;
import com.ProyectoBici.proyecto.repository.UsuarioRepository;

/**
 * Servicio encargado de la lógica de negocio del módulo de bicicletas.
 * Administra la consulta de tarjetas con estado de desgaste, la búsqueda por propietario
 * y la validación, persistencia y almacenamiento de fotografías al registrar una bicicleta.
 */
@Service
public class BicicletaService {

    private static final long MAX_FOTO_BYTES = 5L * 1024 * 1024; // 5 MB
    private static final Set<String> EXTENSIONES_FOTO = Set.of("jpg", "jpeg", "png");
    private static final Set<String> TIPOS_FOTO = Set.of("image/jpeg", "image/png");
    private static final BigDecimal MAX_KM = new BigDecimal("99999999.99"); // límite de DECIMAL(10,2)
    private static final int LIMITE_BICIS_GRATIS = 2;
    private static final String REGEX_MODELO = "^[A-Za-z0-9ÁÉÍÓÚáéíóúÑñ .\\-]{2,80}$";
    private static final String REGEX_SERIE = "^[A-Za-z0-9\\-]{5,30}$";

    @Autowired
    private BicicletaRepository bicicletaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DesgasteComponenteService desgasteComponenteService;

    // Carpeta donde se guardan las fotos (ver application.properties)
    @Value("${bikecare.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Consulta todas las bicicletas pertenecientes a un usuario e incluye el estado de desgaste de sus componentes activos.
     *
     * @param idUsuario Identificador único del usuario ciclista.
     * @return Lista de objetos {@link BicicletaCard} con la bicicleta y el listado de componentes evaluados.
     */
    @Transactional(readOnly = true)
    public List<BicicletaCard> listarConComponentes(int idUsuario) {
        List<BicicletaCard> cards = new ArrayList<>();
        for (Bicicleta bici : bicicletaRepository.listarDeUsuario(idUsuario)) {
            cards.add(new BicicletaCard(bici, desgasteComponenteService.estadoDeComponentes(bici.getIdBicicleta())));
        }
        return cards;
    }

    /**
     * Busca una bicicleta por su identificador asegurando que pertenezca al usuario especificado.
     *
     * @param idBici Identificador único de la bicicleta.
     * @param idUsuario Identificador del usuario propietario.
     * @return Un {@link Optional} con la bicicleta si fue hallada y es de su propiedad, o vacío en caso contrario.
     */
    public Optional<Bicicleta> buscarDeUsuario(int idBici, int idUsuario) {
        return bicicletaRepository.buscarDeUsuario(idBici, idUsuario);
    }

    /**
     * Valida los datos del formulario, guarda la fotografía en el servidor y persiste una nueva bicicleta.
     *
     * @param usuarioSesion Entidad del ciclista en sesión.
     * @param form Objeto {@link BicicletaForm} que contiene los campos enviados desde la vista.
     * @return La entidad {@link Bicicleta} guardada en base de datos.
     * @throws IllegalArgumentException Si algún dato requerido está vacío, supera límites de plan gratuito,
     *                                  el modelo/serie tiene formato inválido, el año está fuera del rango [1900-2100],
     *                                  el kilometraje es negativo o supera el límite, o si el archivo de foto es inválido.
     */
    @Transactional
    public Bicicleta registrar(Usuario usuarioSesion, BicicletaForm form) {
        Usuario usuarioReal = usuarioRepository.findById(usuarioSesion.getId_usuario())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        int bicisActuales = bicicletaRepository.listarDeUsuario(usuarioReal.getId_usuario()).size();
        if (!usuarioReal.isEsPremium() && bicisActuales >= LIMITE_BICIS_GRATIS) {
            throw new IllegalArgumentException(
                    "Alcanzaste el límite de " + LIMITE_BICIS_GRATIS + " bicicletas del plan gratuito. Actualiza a Pro para registrar más.");
        }

        Bicicleta bici = new Bicicleta();
        bici.setUsuario(usuarioReal);
        bici.setMarca(textoObligatorio(form.getMarca(), 80, "La marca"));

        String modelo = textoObligatorio(form.getModelo(), 80, "El modelo");
        if (!modelo.matches(REGEX_MODELO)) {
            throw new IllegalArgumentException("El modelo solo puede contener letras, números, espacios, puntos o guiones (mín. 2 caracteres).");
        }
        bici.setModelo(modelo);

        bici.setTipo(textoObligatorio(form.getTipo(), 50, "El tipo de bicicleta"));

        Integer anio = form.getAnio();
        if (anio != null && (anio < 1900 || anio > 2100)) {
            throw new IllegalArgumentException("El año debe estar entre 1900 y 2100.");
        }
        bici.setAnio(anio);

        String serie = textoOpcional(form.getNumeroSerie(), 30, "El número de serie");
        if (serie != null) {
            serie = serie.toUpperCase(Locale.ROOT);
            if (!serie.matches(REGEX_SERIE)) {
                throw new IllegalArgumentException("El número de serie debe ser alfanumérico (5 a 30 caracteres, sin espacios).");
            }
        }
        bici.setNumeroSerie(serie);

        bici.setTerrenoHabitual(textoOpcional(form.getTerrenoHabitual(), 100, "El terreno"));

        BigDecimal km = form.getKilometrajeTotal();
        if (km == null) {
            throw new IllegalArgumentException("El kilometraje actual es obligatorio.");
        }
        if (km.signum() < 0 || km.compareTo(MAX_KM) > 0) {
            throw new IllegalArgumentException("El kilometraje no es válido.");
        }
        bici.setKilometrajeTotal(km.setScale(2, RoundingMode.HALF_UP));

        BigDecimal peso = form.getPeso();
        if (peso != null) {
            if (peso.signum() < 0 || peso.compareTo(new BigDecimal("999.99")) > 0) {
                throw new IllegalArgumentException("El peso no es válido.");
            }
            peso = peso.setScale(2, RoundingMode.HALF_UP);
        }
        bici.setPeso(peso);

        bici.setTransmision(textoOpcional(form.getTransmision(), 30, "La transmisión"));
        bici.setTamanoCuadro(textoOpcional(form.getTamanoCuadro(), 10, "El tamaño de cuadro"));
        bici.setNotas(textoOpcional(form.getNotas(), 500, "Las notas"));

        // La foto se guarda al final, cuando el resto ya está validado
        bici.setFoto(guardarFoto(form.getFoto()));

        return bicicletaRepository.save(bici);
    }

    // ---------- helpers ----------
    private String textoObligatorio(String valor, int max, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        return textoOpcional(valor, max, campo);
    }

    // Vacío -> null. Si se pasa del largo de la columna, avisa.
    private String textoOpcional(String valor, int max, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpio = valor.trim();
        if (limpio.length() > max) {
            throw new IllegalArgumentException(campo + " no puede pasar de " + max + " caracteres.");
        }
        return limpio;
    }

    // Guarda la imagen en disco con nombre aleatorio y devuelve solo el nombre del archivo
    private String guardarFoto(MultipartFile foto) {
        if (foto == null || foto.isEmpty()) {
            return null;
        }
        if (foto.getSize() > MAX_FOTO_BYTES) {
            throw new IllegalArgumentException("La foto no puede pesar más de 5 MB.");
        }
        String original = foto.getOriginalFilename() == null ? "" : foto.getOriginalFilename();
        int punto = original.lastIndexOf('.');
        String extension = punto < 0 ? "" : original.substring(punto + 1).toLowerCase(Locale.ROOT);
        String tipo = foto.getContentType() == null ? "" : foto.getContentType().toLowerCase(Locale.ROOT);
        if (!EXTENSIONES_FOTO.contains(extension) || !TIPOS_FOTO.contains(tipo)) {
            throw new IllegalArgumentException("La foto debe ser JPG o PNG.");
        }
        try {
            Path carpeta = Paths.get(uploadDir, "bicicletas").toAbsolutePath().normalize();
            Files.createDirectories(carpeta);
            String nombre = UUID.randomUUID() + "." + extension;
            try (InputStream in = foto.getInputStream()) {
                Files.copy(in, carpeta.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
            }
            return nombre;
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo guardar la foto. Inténtalo de nuevo.");
        }
    }
}