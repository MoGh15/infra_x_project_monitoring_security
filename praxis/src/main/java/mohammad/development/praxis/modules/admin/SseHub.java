package mohammad.development.praxis.modules.admin;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseHub {

    private final Set<SseEmitter> emitters = ConcurrentHashMap.newKeySet();

    public SseEmitter register() {
        SseEmitter emitter = new SseEmitter(0L); // kein Timeout
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        // optional: sofort "hello" senden
        try {
            emitter.send(SseEmitter.event()
                    .name("hello")
                    .data("connected@" + Instant.now()));
        } catch (IOException ignored) {
        }

        return emitter;
    }

    public void sendCreated(Object payload) {
        send("created", payload);
    }

    public void sendUpdated(Object payload) {
        send("updated", payload);
    }

    private void send(String eventName, Object payload) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(payload));
            } catch (IOException | IllegalStateException e) {
                emitters.remove(emitter);
            }
        }
    }
}
