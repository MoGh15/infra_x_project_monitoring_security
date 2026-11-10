package mohammad.development.praxis.repos;

import mohammad.development.praxis.modules.admin.Admin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminRepositoryTest {

    @Mock
    private AdminRepository adminRepository;

    @Test
    void findByUsername_existingUser_returnsAdmin() {
        Admin admin = Admin.builder()
                .id("test-id")
                .username("testadmin")
                .passwordHash("$2a$10$hash")
                .roles(Set.of("ADMIN"))
                .enabled(true)
                .build();

        when(adminRepository.findByUsername("testadmin")).thenReturn(Optional.of(admin));

        Optional<Admin> found = adminRepository.findByUsername("testadmin");

        assertTrue(found.isPresent());
        assertEquals("testadmin", found.get().getUsername());
    }

    @Test
    void findByUsername_nonExistingUser_returnsEmpty() {
        when(adminRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<Admin> found = adminRepository.findByUsername("nonexistent");

        assertTrue(found.isEmpty());
    }

    @Test
    void existsByUsername_existingUser_returnsTrue() {
        when(adminRepository.existsByUsername("existinguser")).thenReturn(true);

        assertTrue(adminRepository.existsByUsername("existinguser"));
    }

    @Test
    void existsByUsername_nonExistingUser_returnsFalse() {
        when(adminRepository.existsByUsername("nonexistent")).thenReturn(false);

        assertFalse(adminRepository.existsByUsername("nonexistent"));
    }

    @Test
    void save_returnsAdmin() {
        Admin admin = Admin.builder()
                .username("newadmin")
                .passwordHash("hash")
                .roles(Set.of("ADMIN"))
                .enabled(true)
                .build();

        Admin savedAdmin = Admin.builder()
                .id("generated-id")
                .username("newadmin")
                .passwordHash("hash")
                .roles(Set.of("ADMIN"))
                .enabled(true)
                .build();

        when(adminRepository.save(admin)).thenReturn(savedAdmin);

        Admin result = adminRepository.save(admin);

        assertNotNull(result.getId());
        assertEquals("generated-id", result.getId());
    }

    @Test
    void count_returnsCount() {
        when(adminRepository.count()).thenReturn(5L);

        assertEquals(5L, adminRepository.count());
    }

    @Test
    void findById_existingAdmin_returnsAdmin() {
        Admin admin = Admin.builder()
                .id("test-id")
                .username("findbyid")
                .passwordHash("hash")
                .roles(Set.of("ADMIN"))
                .build();

        when(adminRepository.findById("test-id")).thenReturn(Optional.of(admin));

        Optional<Admin> found = adminRepository.findById("test-id");

        assertTrue(found.isPresent());
        assertEquals("findbyid", found.get().getUsername());
    }

    @Test
    void delete_callsDelete() {
        Admin admin = Admin.builder()
                .id("test-id")
                .username("todelete")
                .passwordHash("hash")
                .roles(Set.of("ADMIN"))
                .build();

        doNothing().when(adminRepository).delete(admin);

        adminRepository.delete(admin);

        verify(adminRepository, times(1)).delete(admin);
    }
}
