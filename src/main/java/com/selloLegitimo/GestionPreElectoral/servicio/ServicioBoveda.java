package com.selloLegitimo.GestionPreElectoral.servicio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.selloLegitimo.GestionPreElectoral.modelo.CeremonySession;
import com.selloLegitimo.GestionPreElectoral.modelo.ClaveroKeyRecord;
import com.selloLegitimo.GestionPreElectoral.modelo.EstadoCeremonia;
import com.selloLegitimo.GestionPreElectoral.modelo.ListaBlanca;
import com.selloLegitimo.GestionPreElectoral.modelo.TipoAccionAuditoria;
import com.selloLegitimo.GestionPreElectoral.modelo.TipoCeremonia;
import com.selloLegitimo.GestionPreElectoral.repositorio.CeremonySessionRepositorio;
import com.selloLegitimo.GestionPreElectoral.repositorio.ClaveroKeyRecordRepositorio;
import com.selloLegitimo.GestionPreElectoral.repositorio.ListaBlancaRepositorio;

@Service
public class ServicioBoveda {

    private static final Logger logger = LoggerFactory.getLogger(ServicioBoveda.class);

    private final ClaveroKeyRecordRepositorio claveroRepositorio;
    private final CeremonySessionRepositorio ceremonyRepositorio;
    private final ListaBlancaRepositorio listaBlancaRepositorio;
    private final ServicioAuditoria servicioAuditoria;

    public ServicioBoveda(ClaveroKeyRecordRepositorio claveroRepositorio,
                          CeremonySessionRepositorio ceremonyRepositorio,
                          ListaBlancaRepositorio listaBlancaRepositorio,
                          ServicioAuditoria servicioAuditoria) {
        this.claveroRepositorio = claveroRepositorio;
        this.ceremonyRepositorio = ceremonyRepositorio;
        this.listaBlancaRepositorio = listaBlancaRepositorio;
        this.servicioAuditoria = servicioAuditoria;
    }

    @Transactional
    public CeremonySession iniciarCeremonia(String initiatedByDoc, TipoCeremonia tipo) {
        Optional<ListaBlanca> optInitiator = listaBlancaRepositorio.findByNumeroDocumento(initiatedByDoc);
        if (optInitiator.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado: " + initiatedByDoc);
        }

        ListaBlanca initiator = optInitiator.get();
        if (!"SUPERADMIN".equalsIgnoreCase(initiator.getRol()) &&
            !"MAGISTRADO".equalsIgnoreCase(initiator.getRol())) {
            throw new SecurityException("Solo SUPERADMIN o MAGISTRADO pueden iniciar ceremonias");
        }

        CeremonySession ceremony = new CeremonySession();
        ceremony.setCeremonyType(tipo);
        ceremony.setInitiatedBy(initiator);
        ceremony.setStatus(EstadoCeremonia.PENDING);
        ceremony.setRequiredShards(3);
        ceremony.setSubmittedShards(0);
        ceremony.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        ceremony = ceremonyRepositorio.save(ceremony);

        logAudit(initiatedByDoc, TipoAccionAuditoria.CEREMONY_INITIATE, ceremony.getId().toString(), null);

        logger.info("[CEREMONY] Iniciada por {} tipo={} id={}",
            initiatedByDoc, tipo, ceremony.getId());
        return ceremony;
    }

    @Transactional
    public Optional<CeremonySession> submitShard(UUID ceremonyId, String shardValue, String submitterDoc) {
        Optional<CeremonySession> optCeremony = ceremonyRepositorio.findById(ceremonyId);
        if (optCeremony.isEmpty()) {
            return Optional.empty();
        }

        CeremonySession ceremony = optCeremony.get();
        if (ceremony.getStatus() == EstadoCeremonia.COMPLETED ||
            ceremony.getStatus() == EstadoCeremonia.ABORTED) {
            logger.warn("[CEREMONY] Ceremonia {} ya finalizada, no se aceptan shards", ceremonyId);
            return Optional.empty();
        }

        if (ceremony.isExpired()) {
            ceremony.setStatus(EstadoCeremonia.ABORTED);
            ceremonyRepositorio.save(ceremony);
            return Optional.of(ceremony);
        }

        Optional<ListaBlanca> optSubmitter = listaBlancaRepositorio.findByNumeroDocumento(submitterDoc);
        if (optSubmitter.isEmpty()) {
            return Optional.empty();
        }

        ListaBlanca submitter = optSubmitter.get();

        // TODO: reemplazar validación de shards simulada con reconstrucción real de Shamir Secret Sharing
        // Simulado: aceptar valores de shard de prueba
        boolean mockValid = shardValue.startsWith("MOCK_SHARD_INDEX_");

        if (!mockValid) {
            Optional<ClaveroKeyRecord> optRecord =
                claveroRepositorio.findByMagistradoIdAndShardIndex(submitter.getId(), 1);
            if (optRecord.isEmpty()) {
                logger.warn("[CEREMONY] No se encontro ClaveroKeyRecord para submitter {}", submitterDoc);
                return Optional.empty();
            }

            ClaveroKeyRecord record = optRecord.get();
            String submittedFingerprint = sha256(shardValue);

            if (!record.getShardFingerprint().equalsIgnoreCase(submittedFingerprint)) {
                logger.warn("[CEREMONY] Shard no valido para {} - fingerprint mismatch", submitterDoc);
                return Optional.empty();
            }

            record.setLastUsedCeremonyId(ceremonyId);
            claveroRepositorio.save(record);
        }

        ceremony.setSubmittedShards(ceremony.getSubmittedShards() + 1);

        logger.info("[MOCK] Shard aceptado de {} para ceremonia {} ({}/{})",
            submitterDoc, ceremonyId, ceremony.getSubmittedShards(), ceremony.getRequiredShards());

        if (ceremony.isReadyToActivate()) {
            ceremony.setStatus(EstadoCeremonia.ACTIVE);
            ceremony.setActivatedAt(LocalDateTime.now());
            logAudit(submitterDoc, TipoAccionAuditoria.CEREMONY_COMPLETED, ceremonyId.toString(), null);
            logger.info("[CEREMONY] Ceremonia {} activada - todos los shards recibidos", ceremonyId);
        } else {
            logAudit(submitterDoc, TipoAccionAuditoria.SHARD_SUBMITTED, ceremonyId.toString(), null);
        }

        ceremony = ceremonyRepositorio.save(ceremony);
        return Optional.of(ceremony);
    }

    public Optional<CeremonySession> getCeremoniaStatus(UUID ceremonyId) {
        return ceremonyRepositorio.findById(ceremonyId);
    }

    @Transactional
    public Optional<CeremonySession> abortCeremonia(UUID ceremonyId, String actorDoc) {
        Optional<CeremonySession> optCeremony = ceremonyRepositorio.findById(ceremonyId);
        if (optCeremony.isEmpty()) {
            return Optional.empty();
        }

        CeremonySession ceremony = optCeremony.get();
        if (ceremony.getStatus() == EstadoCeremonia.COMPLETED) {
            logger.warn("[CEREMONY] No se puede abortar ceremonia ya completada {}", ceremonyId);
            return Optional.empty();
        }

        ceremony.setStatus(EstadoCeremonia.ABORTED);
        ceremony = ceremonyRepositorio.save(ceremony);

        logAudit(actorDoc, TipoAccionAuditoria.CEREMONY_ABORT, ceremonyId.toString(), null);
        logger.info("[CEREMONY] Ceremonia {} abortada por {}", ceremonyId, actorDoc);

        return Optional.of(ceremony);
    }

    private String sha256(String input) {
        try {
            MessageDigest d = MessageDigest.getInstance("SHA-256");
            byte[] hash = d.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return "";
        }
    }

    private void logAudit(String actorId, TipoAccionAuditoria action, String entityId, String ipAddress) {
        try {
            if (servicioAuditoria != null) {
                servicioAuditoria.logEvent(actorId, action, "CEREMONY", entityId, ipAddress, null);
            }
        } catch (Exception e) {
            System.out.println("[AUDIT-STUB] " + action + " actor=" + actorId + " entity=" + entityId);
        }
    }
}