package com.selloLegitimo.GestionPreElectoral.servicio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selloLegitimo.GestionPreElectoral.dto.MetricasListaBlancaDto;
import com.selloLegitimo.GestionPreElectoral.modelo.ListaBlanca;
import com.selloLegitimo.GestionPreElectoral.modelo.ListaBlancaAuditoria;
import com.selloLegitimo.GestionPreElectoral.repositorio.ListaBlancaAuditoriaRepositorio;
import com.selloLegitimo.GestionPreElectoral.repositorio.ListaBlancaRepositorio;

@Service
public class ServicioListaBlanca {

    private final ListaBlancaRepositorio repo;
    private final ListaBlancaAuditoriaRepositorio auditoriaRepo;
    private final EntityManager em;
    private final ObjectMapper mapper = new ObjectMapper();

    public ServicioListaBlanca(ListaBlancaRepositorio repo, ListaBlancaAuditoriaRepositorio auditoriaRepo, EntityManager em) {
        this.repo = repo;
        this.auditoriaRepo = auditoriaRepo;
        this.em = em;
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
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            List<ListaBlanca> lista = repo.listarActivosOrdenados();
            StringBuilder sb = new StringBuilder();
            for (ListaBlanca l : lista) {
                sb.append(l.getHashBiometricoFacial() == null ? "" : l.getHashBiometricoFacial());
                sb.append(l.getNumeroDocumento() == null ? "" : l.getNumeroDocumento());
            }
            byte[] hash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error calculando hash de integridad", ex);
        }
    }

    @Transactional
    public void modificarEmergencia(String ciudadanoId, String nuevaZona, String justificacion, Object firmas) {
        // Desactivar triggers temporalmente a nivel de sesión para permitir modificación de emergencia
        em.createNativeQuery("SET LOCAL session_replication_role = 'replica'").executeUpdate();

        Optional<ListaBlanca> opt = repo.findByCiudadanoId(ciudadanoId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Ciudadano no encontrado en lista blanca: " + ciudadanoId);
        }
        ListaBlanca registro = opt.get();
        registro.setZonaInscripcion(nuevaZona);
        repo.save(registro);

        // Generar version hash para auditoria
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String base = ciudadanoId + "|" + nuevaZona + "|" + LocalDateTime.now().toString();
            byte[] h = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : h) {
                hex.append(String.format("%02x", b));
            }
            ListaBlancaAuditoria audit = new ListaBlancaAuditoria();
            audit.setId(UUID.randomUUID());
            audit.setListaBlancaId(registro.getId());
            audit.setJustificacion(justificacion);
            // firmas se serializa a JSON
            String firmasJson = firmas == null ? null : mapper.writeValueAsString(firmas);
            audit.setFirmasJson(firmasJson);
            audit.setVersionHash(hex.toString());
            audit.setFechaModificacion(LocalDateTime.now());
            auditoriaRepo.save(audit);
        } catch (Exception ex) {
            throw new RuntimeException("Error registrando auditoria de emergencia", ex);
        }
    }
}
