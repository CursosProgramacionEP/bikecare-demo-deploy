package com.ProyectoBici.proyecto.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.springframework.stereotype.Service;

/**
 * Calcula la distancia recorrida contenida en un archivo GPX.
 * <p>
 * Reglas:
 * <ul>
 *   <li>Se usan los puntos de track ({@code trk/trkseg/trkpt}). Si el archivo no tiene tracks,
 *       se usan los puntos de ruta planificada ({@code rte/rtept}).</li>
 *   <li>La distancia se suma entre puntos consecutivos <b>del mismo segmento</b>; no se suma el
 *       salto entre segmentos distintos (por ejemplo, una pausa del GPS).</li>
 *   <li>Los waypoints sueltos ({@code wpt}) se ignoran.</li>
 *   <li>La distancia entre puntos es la ortodrómica (fórmula de Haversine), sin considerar la elevación.</li>
 * </ul>
 * El parser está endurecido contra XXE (DTD y entidades externas deshabilitadas).
 * No sabe nada de HTTP ni de sesión: solo recibe un {@link InputStream}.
 */
@Service
public class GpxService {

    /** Radio medio de la Tierra en kilómetros. */
    private static final double RADIO_TIERRA_KM = 6371.0088;

    /**
     * Lee un GPX y devuelve la distancia total en kilómetros, con 2 decimales.
     *
     * @param gpx Contenido del archivo GPX.
     * @return Distancia total en km (escala 2, redondeo HALF_UP).
     * @throws IllegalArgumentException Si el archivo no es un GPX válido, tiene coordenadas inválidas
     *                                  o no contiene puntos suficientes para medir una distancia.
     */
    public BigDecimal calcularDistanciaKm(InputStream gpx) {
        if (gpx == null) {
            throw new IllegalArgumentException("No se recibió el archivo GPX.");
        }

        double kmTrack = 0;
        double kmRuta = 0;
        int puntosTrack = 0;
        int puntosRuta = 0;

        boolean raizGpx = false;
        boolean enTrack = false;
        boolean enRuta = false;
        // Último punto del segmento actual (NaN = aún no hay punto previo en el segmento).
        double prevLat = Double.NaN;
        double prevLon = Double.NaN;

        XMLStreamReader reader = null;
        try {
            XMLInputFactory factory = XMLInputFactory.newFactory();
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);

            reader = factory.createXMLStreamReader(gpx);

            while (reader.hasNext()) {
                int evento = reader.next();

                if (evento == XMLStreamConstants.START_ELEMENT) {
                    String nombre = reader.getLocalName();

                    if (!raizGpx) {
                        if (!"gpx".equals(nombre)) {
                            throw new IllegalArgumentException("El archivo no es un GPX válido.");
                        }
                        raizGpx = true;
                        continue;
                    }

                    switch (nombre) {
                        case "trk" -> enTrack = true;
                        case "rte" -> {
                            enRuta = true;
                            prevLat = Double.NaN;
                            prevLon = Double.NaN;
                        }
                        case "trkseg" -> {
                            prevLat = Double.NaN;
                            prevLon = Double.NaN;
                        }
                        case "trkpt", "rtept" -> {
                            boolean esTrack = "trkpt".equals(nombre) && enTrack;
                            boolean esRuta = "rtept".equals(nombre) && enRuta;
                            if (esTrack || esRuta) {
                                double lat = leerCoordenada(reader, "lat", -90, 90);
                                double lon = leerCoordenada(reader, "lon", -180, 180);
                                if (!Double.isNaN(prevLat)) {
                                    double d = haversineKm(prevLat, prevLon, lat, lon);
                                    if (esTrack) {
                                        kmTrack += d;
                                    } else {
                                        kmRuta += d;
                                    }
                                }
                                if (esTrack) {
                                    puntosTrack++;
                                } else {
                                    puntosRuta++;
                                }
                                prevLat = lat;
                                prevLon = lon;
                            }
                        }
                        default -> {
                            // Otros elementos (ele, time, extensions, metadata...) se ignoran.
                        }
                    }
                } else if (evento == XMLStreamConstants.END_ELEMENT) {
                    String nombre = reader.getLocalName();
                    if ("trk".equals(nombre)) {
                        enTrack = false;
                    } else if ("rte".equals(nombre)) {
                        enRuta = false;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new IllegalArgumentException("El archivo no es un GPX válido.", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException ignored) {
                    // nada que hacer
                }
            }
            try {
                gpx.close();
            } catch (IOException ignored) {
                // nada que hacer
            }
        }

        if (!raizGpx) {
            throw new IllegalArgumentException("El archivo no es un GPX válido.");
        }

        double km;
        int puntos;
        if (puntosTrack > 0) {
            km = kmTrack;
            puntos = puntosTrack;
        } else {
            km = kmRuta;
            puntos = puntosRuta;
        }

        if (puntos < 2) {
            throw new IllegalArgumentException(
                    "El archivo GPX no contiene suficientes puntos de recorrido para calcular la distancia.");
        }

        return BigDecimal.valueOf(km).setScale(2, RoundingMode.HALF_UP);
    }

    private double leerCoordenada(XMLStreamReader reader, String atributo, double min, double max) {
        String valor = reader.getAttributeValue(null, atributo);
        if (valor == null) {
            throw new IllegalArgumentException("El archivo GPX tiene un punto sin coordenadas (" + atributo + ").");
        }
        double numero;
        try {
            numero = Double.parseDouble(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El archivo GPX tiene coordenadas no válidas.");
        }
        if (Double.isNaN(numero) || Double.isInfinite(numero) || numero < min || numero > max) {
            throw new IllegalArgumentException("El archivo GPX tiene coordenadas fuera de rango.");
        }
        return numero;
    }

    /** Distancia ortodrómica entre dos coordenadas (grados) en kilómetros. */
    static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * RADIO_TIERRA_KM * Math.asin(Math.min(1, Math.sqrt(a)));
    }
}
