package mohammad.development.praxis.controllers;

import mohammad.development.praxis.DTOs.LoginRequest;
import mohammad.development.praxis.DTOs.LoginResponse;
import mohammad.development.praxis.modules.admin.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController controller;

    @Test
    void login_validCredentials_returnsToken() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("password");

        Authentication auth = new UsernamePasswordAuthenticationToken("admin", null, Collections.emptyList());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken("admin")).thenReturn("test-jwt-token");

        LoginResponse result = controller.login(loginRequest);

        assertEquals("test-jwt-token", result.getAccessToken());
    }

    @Test
    void login_invalidCredentials_throws401() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("wrong-password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(ResponseStatusException.class, () -> controller.login(loginRequest));
    }

    @Test
    void login_nullUsername_throws401() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(null);
        loginRequest.setPassword("password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(ResponseStatusException.class, () -> controller.login(loginRequest));
    }

    @Test
    void login_emptyUsername_throws401() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("");
        loginRequest.setPassword("password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(ResponseStatusException.class, () -> controller.login(loginRequest));
    }
}
