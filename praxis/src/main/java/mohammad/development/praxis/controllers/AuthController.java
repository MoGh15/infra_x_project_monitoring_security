package mohammad.development.praxis.controllers;

import lombok.RequiredArgsConstructor;
import mohammad.development.praxis.DTOs.LoginRequest;
import mohammad.development.praxis.DTOs.LoginResponse;
import mohammad.development.praxis.modules.admin.JwtService;
import mohammad.development.praxis.modules.metrics.PraxisMetrics;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PraxisMetrics metrics;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            metrics.adminLogin(true);
            return new LoginResponse(jwtService.generateToken(auth.getName()));
        } catch (AuthenticationException ex) {
            metrics.adminLogin(false);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }
}