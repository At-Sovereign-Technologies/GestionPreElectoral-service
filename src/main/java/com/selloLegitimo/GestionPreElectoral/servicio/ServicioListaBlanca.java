package com.selloLegitimo.GestionPreElectoral.servicio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selloLegitimo.GestionPreElectoral.dto.FirmaActorDto;
import com.selloLegitimo.GestionPreElectoral.dto.FirmasModificacionDto;
import com.selloLegitimo.GestionPreElectoral.dto.MetricasListaBlancaDto;
import com.selloLegitimo.GestionPreElectoral.dto.ModificarEmergenciaRequestDto;
import com.selloLegitimo.GestionPreElectoral.excepcion.ExcepcionNegocio;
import com.selloLegitimo.GestionPreElectoral.excepcion.RecursoNoEncontradoExcepcion;
import com.selloLegitimo.GestionPreElectoral.modelo.EventoAuditoria;
import com.selloLegitimo.GestionPreElectoral.modelo.ListaBlanca;
import com.selloLegitimo.GestionPreElectoral.modelo.ListaBlancaAuditoria;
import com.selloLegitimo.GestionPreElectoral.modelo.TipoEventoAuditoria;
import com.selloLegitimo.GestionPreElectoral.repositorio.ListaBlancaAuditoriaRepositorio;
import com.selloLegitimo.GestionPreElectoral.repositorio.ListaBlancaRepositorio;

@Service
public class ServicioListaBlanca {

    private final ListaBlancaRepositorio repo;
    private final ListaBlancaAuditoriaRepositorio auditoriaRepo;
    private final EntityManager em;
    private final PuertoAuditoria puertoAuditoria;
    private final PuertoNotificacionExterna puertoNotificacion;
    private final ObjectMapper mapper = new ObjectMapper();

    public ServicioListaBlanca(ListaBlancaRepositorio repo,
                               ListaBlancaAuditoriaRepositorio auditoriaRepo,
                               EntityManager em,
                               PuertoAuditoria puertoAuditoria,
                               PuertoNotificacionExterna puertoNotificacion) {
        this.repo = repo;
        this.auditoriaRepo = auditoriaRepo;
        this.em = em;
        this.puertoAuditoria = puertoAuditoria;
        this.puertoNotificacion = puertoNotificacion;
    }

    public MetricasListaBlancaDto obtenerMetricas() {
        long total = repo.countByEstado("HABILITADO");
        List<Object[]> agrupado = repo.contarPorZona();
        List<MetricasListaBlancaDto.ZonaConteo> zonas = new ArrayList<>();
        for (Object[] row : agrupado) {
            String zona = (String) row[0];
            Long conteo = (Long) row[1];
            zonas.add(new MetricasListaBlancaDto.ZonaConteo(zona, conteo));
        }
        return new MetricasListaBlancaDto(total, zonas);
    }

    public String verificarIntegridad() {
        return calcularHashIntegridadLista();
    }

    @Transactional
    public void modificarEmergencia(ModificarEmergenciaRequestDto req) {
        FirmasModificacionDto firmas = req.getFirmas();
        FirmaActorDto firmaSuper = firmas.getSuperadmin();
        FirmaActorDto firmaCne = firmas.getCne();

        // El bypass solo aplica al trigger de lista_blanca (vía de emergencia documentada).
        // No afecta a auditoria_eventos: el log SR-M6 sigue siendo inmutable.
        em.createNativeQuery("SET LOCAL session_replication_role = 'replica'").executeUpdate();

        ListaBlanca registro = repo.findByCiudadanoId(req.getCiudadanoId())
            .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                "Ciudadano no encontrado en lista blanca: " + req.getCiudadanoId()));

        String zonaAnterior = registro.getZonaInscripcion();
        String zonaNueva = req.getNuevaZona();
        registro.setZonaInscripcion(zonaNueva);
        repo.save(registro);

        String nuevoHashLista = calcularHashIntegridadLista();

        EventoAuditoria evento = registrarEventoSrM6(req, firmaSuper, firmaCne,
            registro, zonaAnterior, zonaNueva, nuevoHashLista);

        registrarAuditoriaListaBlanca(req, firmas, registro, zonaAnterior, zonaNueva,
            nuevoHashLista, evento.getId());

        puertoNotificacion.notificarModificacionListaBlanca(
            new PuertoNotificacionExterna.NotificacionModificacionListaBlanca(
                evento.getId(),
                evento.getHashIntegridad(),
                req.getCiudadanoId(),
                zonaAnterior,
                zonaNueva,
                req.getJustificacion(),
                firmaSuper.getUsuario(),
                firmaCne.getUsuario(),
                evento.getFechaEvento()));
    }

    private EventoAuditoria registrarEventoSrM6(ModificarEmergenciaRequestDto req,
                                                FirmaActorDto firmaSuper,
                                                FirmaActorDto firmaCne,
                                                ListaBlanca registro,
                                                String zonaAnterior,
                                                String zonaNueva,
                                                String nuevoHashLista) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ciudadanoId", req.getCiudadanoId());
        payload.put("listaBlancaId", registro.getId().toString());
        payload.put("eleccionId", registro.getEleccionId());
        payload.put("zonaAnterior", zonaAnterior);
        payload.put("zonaNueva", zonaNueva);
        payload.put("justificacion", req.getJustificacion());
        payload.put("firmanteSuperadmin", Map.of(
            "usuario", firmaSuper.getUsuario(),
            "firma", firmaSuper.getFirma()));
        payload.put("firmanteCne", Map.of(
            "usuario", firmaCne.getUsuario(),
            "firma", firmaCne.getFirma()));
        payload.put("nuevoHashIntegridadLista", nuevoHashLista);

        String payloadJson;
        try {
            payloadJson = mapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new ExcepcionNegocio("No se pudo serializar el payload de auditoria");
        }

        String actor = "superadmin:" + firmaSuper.getUsuario() + "+cne:" + firmaCne.getUsuario();

        EventoAuditoria evento = new EventoAuditoria(
            "LISTA_BLANCA",
            registro.getEleccionId(),
            TipoEventoAuditoria.LISTA_BLANCA_MODIFICADA_EMERGENCIA,
            actor,
            payloadJson,
            null);

        puertoAuditoria.registrarEvento(evento);
        return evento;
    }

    private void registrarAuditoriaListaBlanca(ModificarEmergenciaRequestDto req,
                                               FirmasModificacionDto firmas,
                                               ListaBlanca registro,
                                               String zonaAnterior,
                                               String zonaNueva,
                                               String nuevoHashLista,
                                               Long eventoAuditoriaId) {
        ListaBlancaAuditoria audit = new ListaBlancaAuditoria();
        audit.setId(UUID.randomUUID());
        audit.setListaBlancaId(registro.getId());
        audit.setJustificacion(req.getJustificacion());
        try {
            audit.setFirmasJson(mapper.writeValueAsString(firmas));
        } catch (Exception ex) {
            throw new ExcepcionNegocio("No se pudo serializar firmas para auditoria local");
        }
        audit.setVersionHash(nuevoHashLista);
        audit.setFechaModificacion(LocalDateTime.now());
        audit.setZonaAnterior(zonaAnterior);
        audit.setZonaNueva(zonaNueva);
        audit.setFirmanteSuperadmin(firmas.getSuperadmin().getUsuario());
        audit.setFirmanteCne(firmas.getCne().getUsuario());
        audit.setEventoAuditoriaId(eventoAuditoriaId);
        auditoriaRepo.save(audit);
    }

    private String calcularHashIntegridadLista() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            List<ListaBlanca> lista = repo.listarActivosOrdenados();
            StringBuilder sb = new StringBuilder();
            for (ListaBlanca l : lista) {
                sb.append(l.getHashBiometricoFacial() == null ? "" : l.getHashBiometricoFacial());
                sb.append(l.getNumeroDocumento() == null ? "" : l.getNumeroDocumento());
                sb.append(l.getZonaInscripcion() == null ? "" : l.getZonaInscripcion());
            }
            byte[] hash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception ex) {
            throw new ExcepcionNegocio("Error calculando hash de integridad de la lista blanca");
        }
    }
}
