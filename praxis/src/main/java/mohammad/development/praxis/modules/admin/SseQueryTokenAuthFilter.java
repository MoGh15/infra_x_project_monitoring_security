package mohammad.development.praxis.modules.admin;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SseQueryTokenAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // nur für SSE endpoint
        return !request.getRequestURI().equals("/api/admin/submissions/stream");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Wenn schon authenticated -> weiter
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getParameter("access_token");
        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtService.extractUsername(token); // <--- deine Methode
        if (username != null) {
            UserDetails user = userDetailsService.loadUserByUsername(username);
            if (jwtService.isValid(token)) {
                var auth = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
