package com.selloLegitimo.GestionPreElectoral.controlador;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.selloLegitimo.GestionPreElectoral.config.VaultAccessGuard;
import com.selloLegitimo.GestionPreElectoral.modelo.CeremonySession;
import com.selloLegitimo.GestionPreElectoral.modelo.TipoCeremonia;
import com.selloLegitimo.GestionPreElectoral.servicio.ServicioBoveda;
import com.selloLegitimo.GestionPreElectoral.util.GeneradorTokenUtil;

import org.springframework.transaction.annotation.Transactional;

/**

* SECURITY ARCHITECTURE NOTE — Logical Air-Gap (US-SR-M7-03)
* 
* Web session tokens (JWT from /auth/login) grant ZERO access to vault operations.
* token.vault_access is hardcoded to false and cannot be elevated through any web flow.
* 
* Vault access requires ALL of the following simultaneously:
* 
    1. A valid web session (Plane A)
* 
    1. Physical presentation of the key shard (Plane B) via X-Vault-Shard header
* 
    1. An ACTIVE CeremonySession that has not expired
* 
* The key shard is NEVER stored in the database in plaintext.
* Only its SHA-256 fingerprint is stored for verification.
* 
* TODO: Replace mock shard handling with:
* 
    - Shamir's Secret Sharing library (e.g. `secrets.js-grempe`)
* 
    - YubiKey HMAC-SHA1 challenge-response or PIV certificate validation
* 
    - HSM integration for shard reconstruction
*/
@RestController
@RequestMapping("/api/v1")
public class ControladorBovedaCeremonia {

    private final ServicioBoveda servicioBoveda;
    private final VaultAccessGuard vaultAccessGuard;

    public ControladorBovedaCeremonia(ServicioBoveda servicioBoveda, VaultAccessGuard vaultAccessGuard) {
        this.servicioBoveda = servicioBoveda;
        this.vaultAccessGuard = vaultAccessGuard;
    }

    @GetMapping("/vault/status")
    public ResponseEntity<?> vaultStatus(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                         @RequestHeader(value = "X-Vault-Shard", required = false) String shard) {
        if (!isAuthenticated(authHeader)) {
            return ResponseEntity.status(401).body(Map.of("error", "Sesion web requerida"));
        }

        if (shard == null || shard.isBlank()) {
            return ResponseEntity.status(403).body(Map.of(
                "error", "Debe proporcionar una clave (shard) en el header X-Vault-Shard para acceder a la bóveda"
            ));
        }

        if (!vaultAccessGuard.hasVaultAccess(authHeader, shard)) {
            return ResponseEntity.status(403).body(Map.of(
                "error", "No hay una ceremonia activa o la clave no es válida. Inicie una ceremonia desde Mis Claves primero."
            ));
        }

        return ResponseEntity.ok(Map.of(
            "status", "VAULT_ONLINE",
            "message", "Boveda accesible - ceremonia activa",
            "vault_access", true
        ));
    }

    @PostMapping("/ceremony/initiate")
    public ResponseEntity<?> initiateCeremony(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                             @RequestBody Map<String, Object> body) {
        if (!isAuthenticated(authHeader)) {
            return ResponseEntity.status(401).body(Map.of("error", "Sesion requerida"));
        }

        String subject = GeneradorTokenUtil.getSubject(authHeader.replace("Bearer ", ""));
        String tipoStr = (String) body.getOrDefault("type", "APERTURA");
        TipoCeremonia tipo = TipoCeremonia.valueOf(tipoStr.toUpperCase());

        try {
            CeremonySession ceremony = servicioBoveda.iniciarCeremonia(subject, tipo);
            return ResponseEntity.ok(Map.of(
                "ceremonyId", ceremony.getId(),
                "status", ceremony.getStatus().name(),
                "requiredShards", ceremony.getRequiredShards(),
                "submittedShards", ceremony.getSubmittedShards(),
                "expiresAt", ceremony.getExpiresAt().toString()
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/ceremony/{id}/submit-shard")
    public ResponseEntity<?> submitShard(@PathVariable UUID id,
                                        @RequestBody Map<String, Object> body,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!isAuthenticated(authHeader)) {
            return ResponseEntity.status(401).body(Map.of("error", "Sesion requerida"));
        }

        String subject = GeneradorTokenUtil.getSubject(authHeader.replace("Bearer ", ""));
        String shardValue = (String) body.get("shard_value");

        if (shardValue == null || shardValue.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "shard_value requerido"));
        }

        var result = servicioBoveda.submitShard(id, shardValue, subject);
        if (result.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Ceremonia no encontrada o shard no valido"));
        }

        CeremonySession ceremony = result.get();
        return ResponseEntity.ok(Map.ofEntries(
            Map.entry("ceremonyId", ceremony.getId()),
            Map.entry("status", ceremony.getStatus().name()),
            Map.entry("requiredShards", ceremony.getRequiredShards()),
            Map.entry("submittedShards", ceremony.getSubmittedShards()),
            Map.entry("activatedAt", ceremony.getActivatedAt() != null ? ceremony.getActivatedAt().toString() : ""),
            Map.entry("expiresAt", ceremony.getExpiresAt() != null ? ceremony.getExpiresAt().toString() : "")
        ));
    }

    @GetMapping("/ceremony/{id}/status")
    public ResponseEntity<?> ceremonyStatus(@PathVariable UUID id) {
        var result = servicioBoveda.getCeremoniaStatus(id);
        if (result.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Ceremonia no encontrada"));
        }

        CeremonySession ceremony = result.get();
        return ResponseEntity.ok(Map.of(
            "ceremonyId", ceremony.getId(),
            "type", ceremony.getCeremonyType().name(),
            "status", ceremony.getStatus().name(),
            "requiredShards", ceremony.getRequiredShards(),
            "submittedShards", ceremony.getSubmittedShards(),
            "progress", ceremony.getSubmittedShards() + "/" + ceremony.getRequiredShards(),
            "activatedAt", ceremony.getActivatedAt() != null ? ceremony.getActivatedAt().toString() : null,
            "expiresAt", ceremony.getExpiresAt().toString(),
            "expired", ceremony.isExpired()
        ));
    }

    @PostMapping("/ceremony/{id}/abort")
    public ResponseEntity<?> abortCeremony(@PathVariable UUID id,
                                           @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!isAuthenticated(authHeader)) {
            return ResponseEntity.status(401).body(Map.of("error", "Sesion requerida"));
        }

        String subject = GeneradorTokenUtil.getSubject(authHeader.replace("Bearer ", ""));
        var result = servicioBoveda.abortCeremonia(id, subject);
        if (result.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Ceremonia no encontrada"));
        }

        return ResponseEntity.ok(Map.of(
            "ceremonyId", id,
            "status", "ABORTED",
            "message", "Ceremonia abortada"
        ));
    }

    private boolean isAuthenticated(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);
        if (GeneradorTokenUtil.isTokenExpired(token)) {
            return false;
        }
        Map<String, Object> payload = GeneradorTokenUtil.parsePayload(token);
        return payload != null && Boolean.TRUE.equals(payload.get("mfa_verified"));
    }
}