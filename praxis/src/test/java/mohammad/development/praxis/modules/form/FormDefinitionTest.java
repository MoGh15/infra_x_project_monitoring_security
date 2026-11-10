package mohammad.development.praxis.modules.form;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FormDefinitionTest {

    @Test
    void formDefinition_noArgsConstructor() {
        FormDefinition formDef = new FormDefinition();

        assertNull(formDef.getId());
        assertNull(formDef.getVersion());
        assertNull(formDef.getSchema());
        assertNull(formDef.getUiConfig());
        assertNull(formDef.getCreatedAt());
        assertNull(formDef.getUpdatedAt());
    }

    @Test
    void formDefinition_settersAndGetters() {
        FormDefinition formDef = new FormDefinition();
        Instant now = Instant.now();

        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("required", new String[]{"firstName", "lastName"});

        Map<String, Object> uiConfig = new HashMap<>();
        uiConfig.put("stepper", true);
        uiConfig.put("steps", 4);

        formDef.setId("form-id");
        formDef.setVersion("v1");
        formDef.setSchema(schema);
        formDef.setUiConfig(uiConfig);
        formDef.setCreatedAt(now);
        formDef.setUpdatedAt(now);

        assertEquals("form-id", formDef.getId());
        assertEquals("v1", formDef.getVersion());
        assertEquals("object", formDef.getSchema().get("type"));
        assertTrue((Boolean) formDef.getUiConfig().get("stepper"));
        assertEquals(4, formDef.getUiConfig().get("steps"));
        assertEquals(now, formDef.getCreatedAt());
        assertEquals(now, formDef.getUpdatedAt());
    }

    @Test
    void formDefinition_complexSchema() {
        FormDefinition formDef = new FormDefinition();

        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> firstName = new HashMap<>();
        firstName.put("type", "string");
        firstName.put("minLength", 2);
        firstName.put("maxLength", 50);
        properties.put("firstName", firstName);

        Map<String, Object> email = new HashMap<>();
        email.put("type", "string");
        email.put("format", "email");
        properties.put("email", email);

        schema.put("properties", properties);

        formDef.setSchema(schema);

        assertEquals("object", formDef.getSchema().get("type"));
        @SuppressWarnings("unchecked")
        Map<String, Object> props = (Map<String, Object>) formDef.getSchema().get("properties");
        assertNotNull(props);
        assertEquals(2, props.size());
    }

    @Test
    void formDefinition_emptyMaps() {
        FormDefinition formDef = new FormDefinition();

        formDef.setSchema(new HashMap<>());
        formDef.setUiConfig(new HashMap<>());

        assertTrue(formDef.getSchema().isEmpty());
        assertTrue(formDef.getUiConfig().isEmpty());
    }

    @Test
    void formDefinition_equals() {
        Instant now = Instant.now();

        FormDefinition fd1 = new FormDefinition();
        fd1.setId("same-id");
        fd1.setVersion("v1");
        fd1.setCreatedAt(now);
        fd1.setUpdatedAt(now);

        FormDefinition fd2 = new FormDefinition();
        fd2.setId("same-id");
        fd2.setVersion("v1");
        fd2.setCreatedAt(now);
        fd2.setUpdatedAt(now);

        assertEquals(fd1, fd2);
        assertEquals(fd1.hashCode(), fd2.hashCode());
    }

    @Test
    void formDefinition_differentVersions() {
        FormDefinition fd1 = new FormDefinition();
        fd1.setVersion("v1");

        FormDefinition fd2 = new FormDefinition();
        fd2.setVersion("v2");

        assertNotEquals(fd1, fd2);
    }

    @Test
    void formDefinition_toString() {
        FormDefinition formDef = new FormDefinition();
        formDef.setId("test-id");
        formDef.setVersion("v1");

        String toString = formDef.toString();

        assertTrue(toString.contains("test-id"));
        assertTrue(toString.contains("v1"));
    }
}

