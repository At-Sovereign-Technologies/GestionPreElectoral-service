package com.selloLegitimo.GestionPreElectoral.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.HexFormat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GeneradorTokenUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final long TEMP_TOKEN_TTL_SECONDS = 300;
    private static final long SESSION_TOKEN_TTL_SECONDS = 3600;

    private GeneradorTokenUtil() {}

    public static String generarTempToken(String subject, String role, String status) {
        long now = Instant.now().getEpochSecond();
        long exp = now + TEMP_TOKEN_TTL_SECONDS;

        Map<String, Object> payload = Map.of(
            "sub", subject,
            "role", role,
            "status", status,
            "type", "temp",
            "iat", now,
            "exp", exp,
            "jti", UUID.randomUUID().toString()
        );

        return construirJWT(payload);
    }

    public static String generarSessionToken(String subject, String role, boolean mfaVerified) {
        long now = Instant.now().getEpochSecond();
        long exp = now + SESSION_TOKEN_TTL_SECONDS;

        Map<String, Object> payload = Map.of(
            "sub", subject,
            "role", role,
            "mfa_verified", mfaVerified,
            "vault_access", false,
            "type", "session",
            "iat", now,
            "exp", exp,
            "jti", UUID.randomUUID().toString()
        );

        return construirJWT(payload);
    }

    private static String construirJWT(Map<String, Object> payload) {
        try {
            String header = MAPPER.writeValueAsString(Map.of("alg", "HS256", "typ", "JWT"));
            String payloadJson = MAPPER.writeValueAsString(payload);

            String signature = Base64.getEncoder().encodeToString(
                sha256(header + "." + payloadJson).getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getUrlEncoder().withoutPadding().encodeToString(header.getBytes(StandardCharsets.UTF_8))
                   + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8))
                   + "." + signature;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error serializando payload JWT", e);
        }
    }

    private static String sha256(String input) {
        try {
            MessageDigest d = MessageDigest.getInstance("SHA-256");
            byte[] hash = d.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    public static Map<String, Object> parsePayload(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length != 3) return null;
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = MAPPER.readValue(payloadBytes, Map.class);
            return payload;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isTokenExpired(String jwt) {
        Map<String, Object> payload = parsePayload(jwt);
        if (payload == null) return true;
        Object exp = payload.get("exp");
        if (exp == null) return true;
        long expEpoch = exp instanceof Integer ? ((Integer) exp).longValue() : ((Number) exp).longValue();
        return Instant.now().getEpochSecond() > expEpoch;
    }

    public static String getSubject(String jwt) {
        Map<String, Object> payload = parsePayload(jwt);
        return payload != null ? (String) payload.get("sub") : null;
    }

    public static String getType(String jwt) {
        Map<String, Object> payload = parsePayload(jwt);
        return payload != null ? (String) payload.get("type") : null;
    }

    public static boolean isTempToken(String jwt) {
        return "temp".equals(getType(jwt));
    }
}