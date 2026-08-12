package mohammad.development.praxis.controllers;

import mohammad.development.praxis.modules.admin.SseHub;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSubmissionsStreamControllerTest {

    @Mock
    private SseHub hub;

    @InjectMocks
    private AdminSubmissionsStreamController controller;

    @Test
    void stream_returnsEmitter() {
        SseEmitter emitter = new SseEmitter(0L);
        when(hub.register()).thenReturn(emitter);

        SseEmitter result = controller.stream();

        assertNotNull(result);
        assertEquals(emitter, result);
    }

    @Test
    void stream_callsHubRegister() {
        SseEmitter emitter = new SseEmitter(0L);
        when(hub.register()).thenReturn(emitter);

        controller.stream();

        org.mockito.Mockito.verify(hub).register();
    }
}
