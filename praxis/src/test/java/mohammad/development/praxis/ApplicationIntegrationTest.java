package mohammad.development.praxis;

import mohammad.development.praxis.modules.admin.JwtService;
import mohammad.development.praxis.modules.admin.SecurityBeans;
import mohammad.development.praxis.modules.admin.SseHub;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationIntegrationTest {

    @Test
    void jwtService_canBeInstantiated() {
        String secret = "test_secret_key_that_is_at_least_32_characters_long";
        JwtService jwtService = new JwtService(secret, 60);

        assertNotNull(jwtService);
        String token = jwtService.generateToken("testuser");
        assertNotNull(token);
        assertTrue(jwtService.isValid(token));
    }

    @Test
    void sseHub_canBeInstantiated() {
        SseHub sseHub = new SseHub();

        assertNotNull(sseHub);
        assertNotNull(sseHub.register());
    }

    @Test
    void securityBeans_passwordEncoder_works() {
        SecurityBeans securityBeans = new SecurityBeans();
        PasswordEncoder encoder = securityBeans.passwordEncoder();

        assertNotNull(encoder);
        String encoded = encoder.encode("password");
        assertTrue(encoder.matches("password", encoded));
    }
}
