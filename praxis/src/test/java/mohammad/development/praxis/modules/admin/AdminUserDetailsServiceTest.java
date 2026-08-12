package mohammad.development.praxis.modules.admin;

import mohammad.development.praxis.repos.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserDetailsServiceTest {

    @Mock
    private AdminRepository adminRepository;

    private AdminUserDetailsService adminUserDetailsService;

    @BeforeEach
    void setUp() {
        adminUserDetailsService = new AdminUserDetailsService(adminRepository);
    }

    @Test
    void loadUserByUsername_existingUser_returnsUserDetails() {
        Admin admin = Admin.builder()
                .id("admin-id")
                .username("admin")
                .passwordHash("$2a$10$hashedpassword")
                .roles(Set.of("ADMIN"))
                .enabled(true)
                .createdAt(Instant.now())
                .build();

        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        UserDetails userDetails = adminUserDetailsService.loadUserByUsername("admin");

        assertNotNull(userDetails);
        assertEquals("admin", userDetails.getUsername());
        assertEquals("$2a$10$hashedpassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_nonExistingUser_throwsException() {
        when(adminRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                adminUserDetailsService.loadUserByUsername("unknown"));
    }

    @Test
    void loadUserByUsername_disabledUser_returnsDisabledUserDetails() {
        Admin admin = Admin.builder()
                .id("admin-id")
                .username("disabled-admin")
                .passwordHash("$2a$10$hashedpassword")
                .roles(Set.of("ADMIN"))
                .enabled(false)
                .createdAt(Instant.now())
                .build();

        when(adminRepository.findByUsername("disabled-admin")).thenReturn(Optional.of(admin));

        UserDetails userDetails = adminUserDetailsService.loadUserByUsername("disabled-admin");

        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
    }

    @Test
    void loadUserByUsername_multipleRoles_returnsAllRoles() {
        Admin admin = Admin.builder()
                .id("admin-id")
                .username("super-admin")
                .passwordHash("$2a$10$hashedpassword")
                .roles(Set.of("ADMIN", "MANAGER"))
                .enabled(true)
                .createdAt(Instant.now())
                .build();

        when(adminRepository.findByUsername("super-admin")).thenReturn(Optional.of(admin));

        UserDetails userDetails = adminUserDetailsService.loadUserByUsername("super-admin");

        assertNotNull(userDetails);
        assertEquals(2, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")));
    }

    @Test
    void loadUserByUsername_emptyRoles_returnsUserWithNoAuthorities() {
        Admin admin = Admin.builder()
                .id("admin-id")
                .username("no-role-admin")
                .passwordHash("$2a$10$hashedpassword")
                .roles(Set.of())
                .enabled(true)
                .createdAt(Instant.now())
                .build();

        when(adminRepository.findByUsername("no-role-admin")).thenReturn(Optional.of(admin));

        UserDetails userDetails = adminUserDetailsService.loadUserByUsername("no-role-admin");

        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().isEmpty());
    }

    @Test
    void loadUserByUsername_caseMatters() {
        when(adminRepository.findByUsername("Admin")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                adminUserDetailsService.loadUserByUsername("Admin"));
    }
}

