package mohammad.development.praxis.modules.admin;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    @Test
    void securityBeans_passwordEncoder_isBCrypt() {
        SecurityBeans securityBeans = new SecurityBeans();
        PasswordEncoder encoder = securityBeans.passwordEncoder();

        assertNotNull(encoder);
        assertTrue(encoder instanceof BCryptPasswordEncoder);
    }

    @Test
    void securityBeans_passwordEncoder_encodesCorrectly() {
        SecurityBeans securityBeans = new SecurityBeans();
        PasswordEncoder encoder = securityBeans.passwordEncoder();

        String raw = "testPassword";
        String encoded = encoder.encode(raw);

        assertNotEquals(raw, encoded);
        assertTrue(encoder.matches(raw, encoded));
    }

    @Test
    void securityBeans_passwordEncoder_differentHashesForSamePassword() {
        SecurityBeans securityBeans = new SecurityBeans();
        PasswordEncoder encoder = securityBeans.passwordEncoder();

        String raw = "samePassword";
        String hash1 = encoder.encode(raw);
        String hash2 = encoder.encode(raw);

        assertNotEquals(hash1, hash2);
        assertTrue(encoder.matches(raw, hash1));
        assertTrue(encoder.matches(raw, hash2));
    }
}
