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
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SseQueryTokenAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private SseQueryTokenAuthFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new SseQueryTokenAuthFilter(jwtService, userDetailsService);
    }

    @Test
    void shouldNotFilter_nonSseEndpoint_returnsTrue() {
        when(request.getRequestURI()).thenReturn("/api/admin/submissions");

        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_sseEndpoint_returnsFalse() {
        when(request.getRequestURI()).thenReturn("/api/admin/submissions/stream");

        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_otherEndpoints_returnsTrue() {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        assertTrue(filter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/api/submissions");
        assertTrue(filter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/api/admin/submissions/123");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void doFilterInternal_noAccessToken_continuesChain() throws ServletException, IOException {
        when(request.getParameter("access_token")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_blankAccessToken_continuesChain() throws ServletException, IOException {
        when(request.getParameter("access_token")).thenReturn("   ");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_emptyAccessToken_continuesChain() throws ServletException, IOException {
        when(request.getParameter("access_token")).thenReturn("");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_validToken_setsAuthentication() throws ServletException, IOException {
        String token = "valid-token";
        UserDetails userDetails = User.builder()
                .username("admin")
                .password("password")
                .roles("ADMIN")
                .build();

        when(request.getParameter("access_token")).thenReturn(token);
        when(jwtService.extractUsername(token)).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtService.isValid(token)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("admin", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilterInternal_invalidToken_continuesWithoutAuthentication() throws ServletException, IOException {
        String token = "invalid-token";

        when(request.getParameter("access_token")).thenReturn(token);
        when(jwtService.extractUsername(token)).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(
                User.builder().username("admin").password("pass").roles("ADMIN").build()
        );
        when(jwtService.isValid(token)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_alreadyAuthenticated_skipsAuthentication() throws ServletException, IOException {
        // Set up existing authentication
        UserDetails existingUser = User.builder()
                .username("existing-user")
                .password("password")
                .roles("USER")
                .build();
        var existingAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                existingUser, null, existingUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // Should retain existing authentication
        assertEquals("existing-user", SecurityContextHolder.getContext().getAuthentication().getName());
        // Should not try to extract username from token
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    void doFilterInternal_nullUsername_continuesWithoutAuthentication() throws ServletException, IOException {
        String token = "token-with-null-username";

        when(request.getParameter("access_token")).thenReturn(token);
        when(jwtService.extractUsername(token)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void doFilterInternal_validToken_setsCorrectAuthorities() throws ServletException, IOException {
        String token = "valid-token";
        UserDetails userDetails = User.builder()
                .username("admin")
                .password("password")
                .roles("ADMIN", "MANAGER")
                .build();

        when(request.getParameter("access_token")).thenReturn(token);
        when(jwtService.extractUsername(token)).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtService.isValid(token)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(2, auth.getAuthorities().size());
    }
}
