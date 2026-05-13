package com.selloLegitimo.GestionPreElectoral.controlador;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1/auth-mock")
public class ControladorAuthMock {

    private static final Map<String, String> OTP_STORE = new ConcurrentHashMap<>();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String cedula = request.get("cedula");
        if (cedula == null || cedula.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cédula requerida"));
        }

        // Generate mock OTP (always "123456")
        OTP_STORE.put(cedula, "123456");

        return ResponseEntity.ok(Map.of(
            "message", "OTP enviado al celular registrado",
            "step", "otp"
        ));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> request) {
        String cedula = request.get("cedula");
        String otp = request.get("otp");

        if (cedula == null || otp == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cédula y OTP requeridos"));
        }

        String storedOtp = OTP_STORE.get(cedula);
        if (storedOtp == null || !storedOtp.equals(otp)) {
            return ResponseEntity.status(401).body(Map.of("message", "OTP inválido"));
        }

        // Generate mock JWT token
        String token = generateMockJwt(cedula);
        
        // Clean up OTP
        OTP_STORE.remove(cedula);

        return ResponseEntity.ok(Map.of(
            "token", token,
            "user", Map.of(
                "cedula", cedula,
                "nombre", "Usuario de Prueba",
                "rol", "ADMINISTRADOR"
            )
        ));
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateOtp(@RequestBody Map<String, String> request) {
        // Alias for login endpoint (frontend compatibility)
        return login(request);
    }

    private String generateMockJwt(String cedula) {
        try {
            String header = MAPPER.writeValueAsString(Map.of("alg", "HS256", "typ", "JWT"));
            String payload = MAPPER.writeValueAsString(Map.of(
                "sub", cedula,
                "name", "Usuario de Prueba",
                "role", "ADMINISTRADOR",
                "iat", Instant.now().getEpochSecond(),
                "exp", Instant.now().plusSeconds(86400).getEpochSecond()
            ));
            
            String signature = Base64.getEncoder().encodeToString(
                ("mock-signature-" + cedula).getBytes()
            );
            
            return Base64.getEncoder().encodeToString(header.getBytes()) + "." +
                   Base64.getEncoder().encodeToString(payload.getBytes()) + "." +
                   signature;
        } catch (Exception e) {
            return "mock-token-" + UUID.randomUUID();
        }
    }
}