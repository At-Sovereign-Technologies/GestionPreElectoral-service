package com.selloLegitimo.GestionPreElectoral.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.selloLegitimo.GestionPreElectoral.modelo.ClaveroKeyRecord;
import com.selloLegitimo.GestionPreElectoral.repositorio.CeremonySessionRepositorio;
import com.selloLegitimo.GestionPreElectoral.repositorio.ClaveroKeyRecordRepositorio;
import com.selloLegitimo.GestionPreElectoral.util.GeneradorTokenUtil;

@Component
public class VaultAccessGuard {

    private final ClaveroKeyRecordRepositorio claveroRepositorio;
    private final CeremonySessionRepositorio ceremonyRepositorio;

    public VaultAccessGuard(ClaveroKeyRecordRepositorio claveroRepositorio,
                             CeremonySessionRepositorio ceremonyRepositorio) {
        this.claveroRepositorio = claveroRepositorio;
        this.ceremonyRepositorio = ceremonyRepositorio;
    }

    @Transactional(readOnly = true)
    public boolean hasVaultAccess(String authHeader, String vaultShard) {
        if (vaultShard == null || vaultShard.isBlank()) {
            return false;
        }

        String token = authHeader;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || GeneradorTokenUtil.isTokenExpired(token)) {
            return false;
        }

        String subject = GeneradorTokenUtil.getSubject(token);
        if (subject == null) {
            return false;
        }

        // Mock shards are always accepted (demo/testing mode)
        if (vaultShard.startsWith("MOCK_SHARD_INDEX_")) {
            return true;
        }

        List<ClaveroKeyRecord> claveros = claveroRepositorio.findAll();
        String expectedFingerprint = sha256(vaultShard);

        for (ClaveroKeyRecord clavero : claveros) {
            String doc = clavero.getMagistrado() != null
                ? clavero.getMagistrado().getNumeroDocumento()
                : null;

            if (subject.equals(doc)) {
                if (clavero.getShardFingerprint().equalsIgnoreCase(expectedFingerprint)) {
                    return !ceremonyRepositorio.findActiveCeremonies().isEmpty();
                }
            }
        }

        return false;
    }

    public String getShardHeader() {
        return "X-Vault-Shard";
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
}