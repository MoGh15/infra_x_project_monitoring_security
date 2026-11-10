package mohammad.development.praxis.modules.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String SECRET = "test_secret_key_that_is_at_least_32_characters_long_for_testing";
    private static final long EXPIRATION_MINUTES = 60;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_MINUTES);
    }

    @Test
    void generateToken_createsValidToken() {
        String token = jwtService.generateToken("testuser");

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length); // JWT has 3 parts
    }

    @Test
    void extractUsername_returnsCorrectUsername() {
        String token = jwtService.generateToken("admin");

        String username = jwtService.extractUsername(token);

        assertEquals("admin", username);
    }

    @Test
    void isValid_validToken_returnsTrue() {
        String token = jwtService.generateToken("user");

        assertTrue(jwtService.isValid(token));
    }

    @Test
    void isValid_invalidToken_returnsFalse() {
        assertFalse(jwtService.isValid("invalid.token.here"));
    }

    @Test
    void isValid_malformedToken_returnsFalse() {
        assertFalse(jwtService.isValid("not-a-jwt"));
    }

    @Test
    void isValid_nullToken_returnsFalse() {
        assertFalse(jwtService.isValid(null));
    }

    @Test
    void isValid_emptyToken_returnsFalse() {
        assertFalse(jwtService.isValid(""));
    }

    @Test
    void isValid_expiredToken_returnsFalse() {
        // Create a service with 0 minutes expiration (immediate expiration)
        JwtService expiredService = new JwtService(SECRET, 0);
        String token = expiredService.generateToken("user");

        // Wait a moment for token to expire (it should expire immediately)
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertFalse(expiredService.isValid(token));
    }

    @Test
    void isValid_tokenWithDifferentSecret_returnsFalse() {
        String token = jwtService.generateToken("user");

        // Create service with different secret
        JwtService otherService = new JwtService(
                "completely_different_secret_that_is_32_chars_minimum",
                EXPIRATION_MINUTES
        );

        assertFalse(otherService.isValid(token));
    }

    @Test
    void generateToken_differentUsers_differentTokens() {
        String token1 = jwtService.generateToken("user1");
        String token2 = jwtService.generateToken("user2");

        assertNotEquals(token1, token2);
    }

    @Test
    void generateToken_sameUser_differentTimes_differentTokens() throws InterruptedException {
        String token1 = jwtService.generateToken("user");
        Thread.sleep(1000); // Wait 1 second
        String token2 = jwtService.generateToken("user");

        // Tokens should be different due to different issuedAt times
        assertNotEquals(token1, token2);
    }

    @Test
    void extractUsername_afterValidation_works() {
        String token = jwtService.generateToken("testuser");

        assertTrue(jwtService.isValid(token));
        assertEquals("testuser", jwtService.extractUsername(token));
    }

    @Test
    void generateToken_withSpecialCharacters_works() {
        String username = "user@domain.com";
        String token = jwtService.generateToken(username);

        assertTrue(jwtService.isValid(token));
        assertEquals(username, jwtService.extractUsername(token));
    }

    @Test
    void generateToken_withUnicodeCharacters_works() {
        String username = "用户名";
        String token = jwtService.generateToken(username);

        assertTrue(jwtService.isValid(token));
        assertEquals(username, jwtService.extractUsername(token));
    }
}


