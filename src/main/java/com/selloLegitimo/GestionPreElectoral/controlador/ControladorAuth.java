package com.selloLegitimo.GestionPreElectoral.controlador;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.selloLegitimo.GestionPreElectoral.dto.LoginRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.MFASetupRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.MFAVerifySolicitudDto;
import com.selloLegitimo.GestionPreElectoral.dto.MFAVerifyRespuestaDto;
import com.selloLegitimo.GestionPreElectoral.dto.UsuarioAutenticadoDto;
import com.selloLegitimo.GestionPreElectoral.modelo.Ciudadano;
import com.selloLegitimo.GestionPreElectoral.modelo.ListaBlanca;
import com.selloLegitimo.GestionPreElectoral.modelo.MetodoMFA;
import com.selloLegitimo.GestionPreElectoral.modelo.TipoAccionAuditoria;
import com.selloLegitimo.GestionPreElectoral.repositorio.CiudadanoRepositorio;
import com.selloLegitimo.GestionPreElectoral.repositorio.ListaBlancaRepositorio;
import com.selloLegitimo.GestionPreElectoral.servicio.ServicioAuditoria;
import com.selloLegitimo.GestionPreElectoral.servicio.ServicioSeguridadAlertas;
import com.selloLegitimo.GestionPreElectoral.util.GeneradorTokenUtil;
import com.selloLegitimo.GestionPreElectoral.util.UtilAutorizacion;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RestController
@RequestMapping("/api/v1/auth")
public class ControladorAuth {

    private static final String MOCK_TOTP_SECRET = "MOCK_TOTP_SECRET";
    private static final int UMBRAL_ALERTAS_FALLIDAS = 5;

    private final ListaBlancaRepositorio listaBlancaRepositorio;
    private final CiudadanoRepositorio ciudadanoRepositorio;
    private final ServicioAuditoria servicioAuditoria;
    private final ServicioSeguridadAlertas servicioAlertas;

    public ControladorAuth(ListaBlancaRepositorio listaBlancaRepositorio,
                           CiudadanoRepositorio ciudadanoRepositorio,
                           ServicioAuditoria servicioAuditoria,
                           ServicioSeguridadAlertas servicioAlertas) {
        this.listaBlancaRepositorio = listaBlancaRepositorio;
        this.ciudadanoRepositorio = ciudadanoRepositorio;
        this.servicioAuditoria = servicioAuditoria;
        this.servicioAlertas = servicioAlertas;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String numeroDocumento = body.get("numeroDocumento") != null ? body.get("numeroDocumento").trim() : null;
        String contrasena = body.get("contrasena");
        String ipAddress = body.get("ipAddress");

        if (numeroDocumento == null || numeroDocumento.isBlank() ||
            contrasena == null || contrasena.isBlank()) {
            return ResponseEntity.badRequest().body(LoginRespuestaDto.failed("Documento y contrasena requeridos"));
        }

        Optional<ListaBlanca> optUsuario = listaBlancaRepositorio.findByNumeroDocumento(numeroDocumento);
        if (optUsuario.isEmpty()) {
            logAuthEvent(numeroDocumento, TipoAccionAuditoria.LOGIN_FAILED, "usuario inexistente", ipAddress);
            return ResponseEntity.status(401).body(LoginRespuestaDto.failed("Credenciales invalidas"));
        }

        ListaBlanca usuario = optUsuario.get();

        if (!validarContrasena(contrasena, usuario.getContrasenaHash())) {
            usuario.setFailedAttempts(usuario.getFailedAttempts() + 1);
            usuario.setLastFailedAt(LocalDateTime.now());
            listaBlancaRepositorio.save(usuario);

            if (usuario.getFailedAttempts() >= UMBRAL_ALERTAS_FALLIDAS) {
                servicioAlertas.alertFailedAttempts(numeroDocumento, usuario.getFailedAttempts());
            }

            logAuthEvent(numeroDocumento, TipoAccionAuditoria.LOGIN_FAILED, numeroDocumento, ipAddress);
            return ResponseEntity.status(401).body(LoginRespuestaDto.failed("Credenciales invalidas"));
        }

        String rol = usuario.getRol() != null ? usuario.getRol() : "ADMINISTRADOR";
        boolean requiereMFA = UtilAutorizacion.requiresMFA(rol);

        if (!requiereMFA) {
            String token = GeneradorTokenUtil.generarSessionToken(numeroDocumento, rol, true);
            usuario.setFailedAttempts(0);
            listaBlancaRepositorio.save(usuario);

            logAuthEvent(numeroDocumento, TipoAccionAuditoria.LOGIN, rol, ipAddress);
            return ResponseEntity.ok(LoginRespuestaDto.success(token, buildUserInfo(usuario)));
        }

        if (!usuario.isMfaEnabled()) {
            String tempToken = GeneradorTokenUtil.generarTempToken(numeroDocumento, rol, "MFA_SETUP_REQUIRED");
            logAuthEvent(numeroDocumento, TipoAccionAuditoria.LOGIN, rol + " (MFA_SETUP_REQUIRED)", ipAddress);
            return ResponseEntity.ok(LoginRespuestaDto.mfaSetupRequired(tempToken, buildUserInfo(usuario)));
        }

        String tempToken = GeneradorTokenUtil.generarTempToken(numeroDocumento, rol, "MFA_CHALLENGE");
        logAuthEvent(numeroDocumento, TipoAccionAuditoria.LOGIN, rol + " (MFA_CHALLENGE)", ipAddress);
        return ResponseEntity.ok(LoginRespuestaDto.mfaChallenge(tempToken, buildUserInfo(usuario)));
    }

    @PostMapping("/mfa/setup")
    public ResponseEntity<?> mfaSetup(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> body) {

        String token = extraerToken(authHeader);
        if (token == null || GeneradorTokenUtil.isTokenExpired(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Token invalido o expirado"));
        }

        String subject = GeneradorTokenUtil.getSubject(token);
        if (subject == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Temp token invalido"));
        }

        Optional<ListaBlanca> optUsuario = listaBlancaRepositorio.findByNumeroDocumento(subject);
        if (optUsuario.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        }

        ListaBlanca usuario = optUsuario.get();

        // TODO: integrar librería TOTP real (e.g. speakeasy, otplib) to generate real secret + QR URI
        usuario.setMfaSecret(MOCK_TOTP_SECRET);
        usuario.setMfaEnabled(true);
        usuario.setMfaMethod(MetodoMFA.TOTP);
        usuario.setMfaConfiguredAt(LocalDateTime.now());
        listaBlancaRepositorio.save(usuario);

        logAuthEvent(subject, TipoAccionAuditoria.MFA_SETUP, subject, null);

        MFASetupRespuestaDto respuesta = MFASetupRespuestaDto.mocked();
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<?> mfaVerify(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body) {

        String token = extraerToken(authHeader);
        if (token == null || GeneradorTokenUtil.isTokenExpired(token)) {
            return ResponseEntity.status(401).body(MFAVerifyRespuestaDto.failed("Token invalido o expirado"));
        }

        String subject = GeneradorTokenUtil.getSubject(token);
        if (subject == null) {
            return ResponseEntity.status(401).body(MFAVerifyRespuestaDto.failed("Temp token invalido"));
        }

        Optional<ListaBlanca> optUsuario = listaBlancaRepositorio.findByNumeroDocumento(subject);
        if (optUsuario.isEmpty()) {
            return ResponseEntity.status(404).body(MFAVerifyRespuestaDto.failed("Usuario no encontrado"));
        }

        ListaBlanca usuario = optUsuario.get();
        String otpCode = body.get("otpCode");

        // TODO: validar otp_code contra mfa_secret usando librería TOTP (e.g. otplib, speakeasy)
        boolean otpValido = otpCode != null && otpCode.matches("\\d{6}");

        if (!otpValido) {
            logAuthEvent(subject, TipoAccionAuditoria.MFA_VERIFY_FAIL, subject, null);
            return ResponseEntity.status(401).body(MFAVerifyRespuestaDto.failed("Codigo MFA invalido"));
        }

        String rol = usuario.getRol() != null ? usuario.getRol() : "ADMINISTRADOR";
        String sessionToken = GeneradorTokenUtil.generarSessionToken(subject, rol, true);

        usuario.setFailedAttempts(0);
        listaBlancaRepositorio.save(usuario);

        logAuthEvent(subject, TipoAccionAuditoria.MFA_VERIFY_SUCCESS, subject, null);

        return ResponseEntity.ok(MFAVerifyRespuestaDto.success(sessionToken));
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateOtp(@RequestBody Map<String, String> request) {
        String cedula = request.get("cedula");
        if (cedula == null || cedula.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cedula requerida"));
        }
        // Alias para login - soporte del campo cedula del frontend
        var body = Map.of(
            "numeroDocumento", cedula,
            "contrasena", request.getOrDefault("password", "")
        );
        return login(body);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extraerToken(authHeader);
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Token requerido"));
        }

        Map<String, Object> payload = GeneradorTokenUtil.parsePayload(token);
        if (payload == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Token invalido"));
        }

        String subject = (String) payload.get("sub");
        if (subject == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Token invalido"));
        }

        Optional<ListaBlanca> optUsuario = listaBlancaRepositorio.findByNumeroDocumento(subject);
        if (optUsuario.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        }

        return ResponseEntity.ok(buildUserInfo(optUsuario.get()));
    }

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private boolean validarContrasena(String raw, String hash) {
        if (hash == null || hash.isBlank()) {
            return false;
        }
        return passwordEncoder.matches(raw, hash);
    }

    private String resolveNombreCompleto(ListaBlanca usuario) {
        try {
            String doc = usuario.getNumeroDocumento();
            if (doc != null) {
                java.util.Optional<Ciudadano> ciudadano = ciudadanoRepositorio.findByNumeroDocumento(doc);
                if (ciudadano.isPresent()) {
                    return ciudadano.get().getNombres() + " " + ciudadano.get().getApellidos();
                }
            }
        } catch (Exception ignored) {}
        return usuario.getNumeroDocumento();
    }

    private Map<String, Object> buildUserInfo(ListaBlanca usuario) {
        return new UsuarioAutenticadoDto(
            usuario.getNumeroDocumento(),
            resolveNombreCompleto(usuario),
            usuario.getRol(),
            usuario.getTelefonoCelular(),
            usuario.getCorreoElectronico(),
            usuario.isMfaEnabled()
        ).toMap();
    }

    private String extraerToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private void logAuthEvent(String actorId, TipoAccionAuditoria action, String entityId, String ipAddress) {
        try {
            if (servicioAuditoria != null) {
                servicioAuditoria.logEvent(actorId, action, "AUTH", entityId, ipAddress, null);
            }
        } catch (Exception e) {
            System.out.println("[AUDIT-STUB] " + action + " actor=" + actorId + " entity=" + entityId);
        }
    }
}