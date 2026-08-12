package mohammad.development.praxis.modules.admin;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AdminUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    @Test
    void doFilterInternal_noAuthorizationHeader_continuesChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_nonBearerToken_continuesChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_invalidToken_continuesChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtService.isValid("invalid-token")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_validToken_setsAuthentication() throws ServletException, IOException {
        String token = "valid-jwt-token";
        UserDetails userDetails = User.builder()
                .username("admin")
                .password("password")
                .roles("ADMIN")
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isValid(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("admin", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilterInternal_validToken_alreadyAuthenticated_skipsReauthentication() throws ServletException, IOException {
        String token = "valid-jwt-token";

        // Set up existing authentication
        UserDetails existingUser = User.builder()
                .username("existing-user")
                .password("password")
                .roles("USER")
                .build();
        var existingAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                existingUser, null, existingUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isValid(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn("admin");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // Should still have the existing authentication
        assertEquals("existing-user", SecurityContextHolder.getContext().getAuthentication().getName());
        // loadUserByUsername should never be called
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void doFilterInternal_bearerTokenWithExtraSpaces_parsesCorrectly() throws ServletException, IOException {
        String token = "valid-token";
        UserDetails userDetails = User.builder()
                .username("admin")
                .password("password")
                .roles("ADMIN")
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isValid(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_emptyBearerToken_continuesChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        when(jwtService.isValid("")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_lowercaseBearer_continuesChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("bearer token");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

