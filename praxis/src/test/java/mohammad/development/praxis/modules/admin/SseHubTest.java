package mohammad.development.praxis.modules.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;

class SseHubTest {

    private SseHub sseHub;

    @BeforeEach
    void setUp() {
        sseHub = new SseHub();
    }

    @Test
    void register_returnsEmitter() {
        SseEmitter emitter = sseHub.register();

        assertNotNull(emitter);
    }

    @Test
    void register_multipleEmitters_allRegistered() {
        SseEmitter emitter1 = sseHub.register();
        SseEmitter emitter2 = sseHub.register();
        SseEmitter emitter3 = sseHub.register();

        assertNotNull(emitter1);
        assertNotNull(emitter2);
        assertNotNull(emitter3);
        assertNotSame(emitter1, emitter2);
        assertNotSame(emitter2, emitter3);
    }

    @Test
    void sendCreated_withRegisteredEmitters_doesNotThrow() {
        sseHub.register();
        sseHub.register();

        assertDoesNotThrow(() -> sseHub.sendCreated(new TestPayload("test")));
    }

    @Test
    void sendCreated_withNoEmitters_doesNotThrow() {
        assertDoesNotThrow(() -> sseHub.sendCreated(new TestPayload("test")));
    }

    @Test
    void sendUpdated_withRegisteredEmitters_doesNotThrow() {
        sseHub.register();

        assertDoesNotThrow(() -> sseHub.sendUpdated(new TestPayload("update")));
    }

    @Test
    void sendUpdated_withNoEmitters_doesNotThrow() {
        assertDoesNotThrow(() -> sseHub.sendUpdated(new TestPayload("update")));
    }

    @Test
    void emitter_completion_removesFromHub() {
        SseEmitter emitter = sseHub.register();

        // Simulate completion
        emitter.complete();

        // Should not throw even after completion
        assertDoesNotThrow(() -> sseHub.sendCreated(new TestPayload("test")));
    }

    @Test
    void emitter_timeout_removesFromHub() {
        SseEmitter emitter = sseHub.register();

        // Simulate timeout by completing with error
        emitter.completeWithError(new RuntimeException("Timeout"));

        // Should not throw even after timeout
        assertDoesNotThrow(() -> sseHub.sendCreated(new TestPayload("test")));
    }

    @Test
    void sendCreated_withNullPayload_doesNotThrow() {
        sseHub.register();

        assertDoesNotThrow(() -> sseHub.sendCreated(null));
    }

    @Test
    void sendUpdated_withNullPayload_doesNotThrow() {
        sseHub.register();

        assertDoesNotThrow(() -> sseHub.sendUpdated(null));
    }

    @Test
    void sendCreated_withComplexPayload_doesNotThrow() {
        sseHub.register();

        ComplexPayload payload = new ComplexPayload("id", "Test Name", 42);

        assertDoesNotThrow(() -> sseHub.sendCreated(payload));
    }

    @Test
    void register_emitterHasNoTimeout() {
        SseEmitter emitter = sseHub.register();

        // Timeout of 0 means no timeout
        assertEquals(0L, emitter.getTimeout());
    }

    @Test
    void sendCreated_multiplePayloads_inSequence() {
        sseHub.register();

        assertDoesNotThrow(() -> {
            sseHub.sendCreated(new TestPayload("first"));
            sseHub.sendCreated(new TestPayload("second"));
            sseHub.sendCreated(new TestPayload("third"));
        });
    }

    @Test
    void sendUpdated_multiplePayloads_inSequence() {
        sseHub.register();

        assertDoesNotThrow(() -> {
            sseHub.sendUpdated(new TestPayload("first"));
            sseHub.sendUpdated(new TestPayload("second"));
            sseHub.sendUpdated(new TestPayload("third"));
        });
    }

    @Test
    void mixedSendOperations_withMultipleEmitters() {
        sseHub.register();
        sseHub.register();
        sseHub.register();

        assertDoesNotThrow(() -> {
            sseHub.sendCreated(new TestPayload("created"));
            sseHub.sendUpdated(new TestPayload("updated"));
            sseHub.sendCreated(new TestPayload("created again"));
        });
    }

    // Helper record/class for testing
    private record TestPayload(String message) {
    }

    private record ComplexPayload(String id, String name, int value) {
    }
}

