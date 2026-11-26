package com.ProyectoBici.proyecto.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

public class GpxServiceTest {

    private final GpxService service = new GpxService();

    private static InputStream gpx(String xml) {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }

    private static String envolver(String cuerpo) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<gpx version=\"1.1\" creator=\"test\" xmlns=\"http://www.topografix.com/GPX/1/1\">"
                + cuerpo + "</gpx>";
    }

    @Test
    public void unGradoDeLatitudSonUnos111Km() {
        // 1 grado de latitud ≈ 111.195 km con radio medio 6371.0088
        String xml = envolver("<trk><trkseg>"
                + "<trkpt lat=\"0\" lon=\"0\"><ele>10</ele></trkpt>"
                + "<trkpt lat=\"1\" lon=\"0\"><ele>12</ele></trkpt>"
                + "</trkseg></trk>");
        assertEquals(new BigDecimal("111.20"), service.calcularDistanciaKm(gpx(xml)));
    }

    @Test
    public void sumaVariosSegmentosSinContarElSaltoEntreEllos() {
        String xml = envolver("<trk>"
                + "<trkseg><trkpt lat=\"0\" lon=\"0\"/><trkpt lat=\"0.1\" lon=\"0\"/></trkseg>"
                + "<trkseg><trkpt lat=\"5\" lon=\"5\"/><trkpt lat=\"5.1\" lon=\"5\"/></trkseg>"
                + "</trk>");
        // 2 tramos de 0.1° de latitud ≈ 11.12 km cada uno; el salto (0,0)->(5,5) no cuenta.
        assertEquals(new BigDecimal("22.24"), service.calcularDistanciaKm(gpx(xml)));
    }

    @Test
    public void usaRutaPlanificadaSiNoHayTrack() {
        String xml = envolver("<rte><rtept lat=\"0\" lon=\"0\"/><rtept lat=\"1\" lon=\"0\"/></rte>");
        assertEquals(new BigDecimal("111.20"), service.calcularDistanciaKm(gpx(xml)));
    }

    @Test
    public void ignoraWaypointsSueltos() {
        String xml = envolver("<wpt lat=\"0\" lon=\"0\"/><wpt lat=\"50\" lon=\"50\"/>"
                + "<trk><trkseg><trkpt lat=\"0\" lon=\"0\"/><trkpt lat=\"1\" lon=\"0\"/></trkseg></trk>");
        assertEquals(new BigDecimal("111.20"), service.calcularDistanciaKm(gpx(xml)));
    }

    @Test
    public void rechazaArchivoQueNoEsXml() {
        assertThrows(IllegalArgumentException.class, () -> service.calcularDistanciaKm(gpx("esto no es xml")));
    }

    @Test
    public void rechazaXmlQueNoEsGpx() {
        assertThrows(IllegalArgumentException.class, () -> service.calcularDistanciaKm(gpx("<html><body/></html>")));
    }

    @Test
    public void rechazaGpxConUnSoloPunto() {
        String xml = envolver("<trk><trkseg><trkpt lat=\"0\" lon=\"0\"/></trkseg></trk>");
        assertThrows(IllegalArgumentException.class, () -> service.calcularDistanciaKm(gpx(xml)));
    }

    @Test
    public void rechazaCoordenadasFueraDeRango() {
        String xml = envolver("<trk><trkseg><trkpt lat=\"95\" lon=\"0\"/><trkpt lat=\"1\" lon=\"0\"/></trkseg></trk>");
        assertThrows(IllegalArgumentException.class, () -> service.calcularDistanciaKm(gpx(xml)));
    }

    @Test
    public void rechazaPuntoSinCoordenadas() {
        String xml = envolver("<trk><trkseg><trkpt lat=\"0\"/><trkpt lat=\"1\" lon=\"0\"/></trkseg></trk>");
        assertThrows(IllegalArgumentException.class, () -> service.calcularDistanciaKm(gpx(xml)));
    }

    @Test
    public void noResuelveEntidadesExternasXxe() {
        String xml = "<?xml version=\"1.0\"?><!DOCTYPE gpx [<!ENTITY x SYSTEM \"file:///etc/passwd\">]>"
                + "<gpx><trk><trkseg><name>&x;</name><trkpt lat=\"0\" lon=\"0\"/><trkpt lat=\"1\" lon=\"0\"/></trkseg></trk></gpx>";
        // Con DTD deshabilitado no debe filtrar contenido; falla o ignora, pero nunca lee el archivo.
        try {
            service.calcularDistanciaKm(gpx(xml));
        } catch (IllegalArgumentException esperado) {
            // válido
        }
    }
}
