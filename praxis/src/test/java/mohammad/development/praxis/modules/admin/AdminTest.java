package mohammad.development.praxis.modules.admin;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AdminTest {

    @Test
    void admin_noArgsConstructor() {
        Admin admin = new Admin();

        assertNull(admin.getId());
        assertNull(admin.getUsername());
        assertNull(admin.getPasswordHash());
        assertNull(admin.getRoles());
        assertTrue(admin.isEnabled());
    }

    @Test
    void admin_allArgsConstructor() {
        Instant now = Instant.now();
        Admin admin = new Admin(
                "id-123",
                "testuser",
                "hashedPassword",
                Set.of("ADMIN"),
                true,
                now,
                now
        );

        assertEquals("id-123", admin.getId());
        assertEquals("testuser", admin.getUsername());
        assertEquals("hashedPassword", admin.getPasswordHash());
        assertEquals(1, admin.getRoles().size());
        assertTrue(admin.getRoles().contains("ADMIN"));
        assertTrue(admin.isEnabled());
        assertEquals(now, admin.getCreatedAt());
        assertEquals(now, admin.getUpdatedAt());
    }

    @Test
    void admin_builder() {
        Instant now = Instant.now();
        Admin admin = Admin.builder()
                .id("builder-id")
                .username("builder-user")
                .passwordHash("$2a$10$hash")
                .roles(Set.of("ADMIN", "MANAGER"))
                .enabled(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals("builder-id", admin.getId());
        assertEquals("builder-user", admin.getUsername());
        assertEquals("$2a$10$hash", admin.getPasswordHash());
        assertEquals(2, admin.getRoles().size());
        assertFalse(admin.isEnabled());
        assertEquals(now, admin.getCreatedAt());
        assertEquals(now, admin.getUpdatedAt());
    }

    @Test
    void admin_settersAndGetters() {
        Admin admin = new Admin();
        Instant created = Instant.now();
        Instant updated = Instant.now();

        admin.setId("set-id");
        admin.setUsername("set-user");
        admin.setPasswordHash("set-hash");
        admin.setRoles(Set.of("USER"));
        admin.setEnabled(true);
        admin.setCreatedAt(created);
        admin.setUpdatedAt(updated);

        assertEquals("set-id", admin.getId());
        assertEquals("set-user", admin.getUsername());
        assertEquals("set-hash", admin.getPasswordHash());
        assertTrue(admin.getRoles().contains("USER"));
        assertTrue(admin.isEnabled());
        assertEquals(created, admin.getCreatedAt());
        assertEquals(updated, admin.getUpdatedAt());
    }

    @Test
    void admin_multipleRoles() {
        Admin admin = Admin.builder()
                .roles(Set.of("ADMIN", "SUPER_ADMIN", "MANAGER"))
                .build();

        assertEquals(3, admin.getRoles().size());
        assertTrue(admin.getRoles().contains("ADMIN"));
        assertTrue(admin.getRoles().contains("SUPER_ADMIN"));
        assertTrue(admin.getRoles().contains("MANAGER"));
    }

    @Test
    void admin_emptyRoles() {
        Admin admin = Admin.builder()
                .roles(Set.of())
                .build();

        assertTrue(admin.getRoles().isEmpty());
    }

    @Test
    void admin_equals_sameValues() {
        Instant now = Instant.now();
        Admin admin1 = Admin.builder()
                .id("same-id")
                .username("user")
                .passwordHash("hash")
                .roles(Set.of("ADMIN"))
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Admin admin2 = Admin.builder()
                .id("same-id")
                .username("user")
                .passwordHash("hash")
                .roles(Set.of("ADMIN"))
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(admin1, admin2);
        assertEquals(admin1.hashCode(), admin2.hashCode());
    }

    @Test
    void admin_equals_differentId() {
        Admin admin1 = Admin.builder().id("id1").build();
        Admin admin2 = Admin.builder().id("id2").build();

        assertNotEquals(admin1, admin2);
    }

    @Test
    void admin_toString() {
        Admin admin = Admin.builder()
                .id("test-id")
                .username("testuser")
                .build();

        String toString = admin.toString();

        assertTrue(toString.contains("test-id"));
        assertTrue(toString.contains("testuser"));
    }
}

