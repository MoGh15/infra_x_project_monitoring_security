package mohammad.development.praxis.controllers;

import lombok.RequiredArgsConstructor;
import mohammad.development.praxis.modules.admin.SseHub;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class AdminSubmissionsStreamController {

    private final SseHub hub;

    @GetMapping(value = "/api/admin/submissions/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return hub.register();
    }
}
