package mohammad.development.praxis.modules.admin;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityBeansTest {

    private final SecurityBeans securityBeans = new SecurityBeans();

    @Test
    void passwordEncoder_returnsBCryptPasswordEncoder() {
        PasswordEncoder encoder = securityBeans.passwordEncoder();

        assertNotNull(encoder);
        assertTrue(encoder instanceof BCryptPasswordEncoder);
    }

    @Test
    void passwordEncoder_encodesPassword() {
        PasswordEncoder encoder = securityBeans.passwordEncoder();

        String password = "testPassword123";
        String encoded = encoder.encode(password);

        assertNotNull(encoded);
        assertNotEquals(password, encoded);
        assertTrue(encoder.matches(password, encoded));
    }

    @Test
    void passwordEncoder_differentEncodings() {
        PasswordEncoder encoder = securityBeans.passwordEncoder();

        String password = "samePassword";
        String encoded1 = encoder.encode(password);
        String encoded2 = encoder.encode(password);

        // BCrypt generates different hashes even for the same password
        assertNotEquals(encoded1, encoded2);
        // But both should match the original password
        assertTrue(encoder.matches(password, encoded1));
        assertTrue(encoder.matches(password, encoded2));
    }

    @Test
    void authenticationManager_returnsManager() throws Exception {
        AuthenticationConfiguration config = mock(AuthenticationConfiguration.class);
        AuthenticationManager mockManager = mock(AuthenticationManager.class);
        when(config.getAuthenticationManager()).thenReturn(mockManager);

        AuthenticationManager result = securityBeans.authenticationManager(config);

        assertNotNull(result);
        assertSame(mockManager, result);
    }

    @Test
    void authenticationManager_delegatesToConfiguration() throws Exception {
        AuthenticationConfiguration config = mock(AuthenticationConfiguration.class);
        AuthenticationManager mockManager = mock(AuthenticationManager.class);
        when(config.getAuthenticationManager()).thenReturn(mockManager);

        securityBeans.authenticationManager(config);

        verify(config).getAuthenticationManager();
    }
}

