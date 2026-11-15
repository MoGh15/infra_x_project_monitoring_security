package mohammad.development.praxis.modules.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import mohammad.development.praxis.modules.admin.SseHub;
import mohammad.development.praxis.modules.patient.Submission;
import mohammad.development.praxis.modules.patient.SubmissionStatus;
import mohammad.development.praxis.repos.SubmissionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Business metrics of the praxis application, exported via /actuator/prometheus.
 * Labels must never contain patient data (GDPR Art. 9).
 */
@Slf4j
@Component
public class PraxisMetrics {

    private final MeterRegistry registry;
    private final SubmissionRepository submissionRepository;

    private final Map<SubmissionStatus, AtomicLong> submissionsByStatus = new EnumMap<>(SubmissionStatus.class);
    private final AtomicLong oldestNewSubmissionAgeSeconds = new AtomicLong();
    private final Timer timeToDone;
    private final DistributionSummary attachmentSize;
    private final Counter attachmentDownloads;

    public PraxisMetrics(MeterRegistry registry, SubmissionRepository submissionRepository, SseHub sseHub) {
        this.registry = registry;
        this.submissionRepository = submissionRepository;

        for (SubmissionStatus status : SubmissionStatus.values()) {
            AtomicLong value = new AtomicLong();
            submissionsByStatus.put(status, value);
            Gauge.builder("praxis.submissions.by.status", value, AtomicLong::get)
                    .description("Current number of submissions per status")
                    .tag("status", status.name())
                    .register(registry);
        }

        Gauge.builder("praxis.submissions.oldest.new.age", oldestNewSubmissionAgeSeconds, AtomicLong::get)
                .description("Age of the oldest submission that is still in status NEW (0 if none)")
                .baseUnit("seconds")
                .register(registry);

        Gauge.builder("praxis.sse.connected.clients", sseHub, SseHub::getConnectedClients)
                .description("Admin dashboards currently connected for live updates (SSE)")
                .register(registry);

        this.timeToDone = Timer.builder("praxis.submission.time.to.done")
                .description("Time from submission until it was marked as DONE")
                .publishPercentileHistogram()
                .minimumExpectedValue(Duration.ofMinutes(1))
                .maximumExpectedValue(Duration.ofDays(14))
                .register(registry);

        this.attachmentSize = DistributionSummary.builder("praxis.submission.attachment.size")
                .description("Size of uploaded attachments")
                .baseUnit("bytes")
                .register(registry);

        this.attachmentDownloads = Counter.builder("praxis.admin.attachment.downloads")
                .description("Attachments opened by staff")
                .register(registry);
    }

    public void submissionCreated(String formVersion, boolean hasAttachments) {
        Counter.builder("praxis.submissions.created")
                .description("Successfully submitted patient forms")
                .tag("form_version", formVersion != null && !formVersion.isBlank() ? formVersion : "unknown")
                .tag("has_attachments", String.valueOf(hasAttachments))
                .register(registry)
                .increment();
    }

    public void submissionRejected(String reason) {
        Counter.builder("praxis.submissions.rejected")
                .description("Rejected patient form submissions")
                .tag("reason", reason)
                .register(registry)
                .increment();
    }

    public void attachmentUploaded(String contentType, long sizeBytes) {
        Counter.builder("praxis.submission.attachments")
                .description("Uploaded attachments")
                .tag("content_type", contentType != null ? contentType : "unknown")
                .register(registry)
                .increment();
        attachmentSize.record(sizeBytes);
    }

    public void statusChanged(Submission submission, SubmissionStatus from, SubmissionStatus to) {
        if (to == null || from == to) {
            return;
        }
        Counter.builder("praxis.submission.status.changes")
                .description("Submission status transitions performed by staff")
                .tag("from", from != null ? from.name() : "unknown")
                .tag("to", to.name())
                .register(registry)
                .increment();
        if (to == SubmissionStatus.DONE && submission.getCreatedAt() != null) {
            Duration duration = Duration.between(submission.getCreatedAt(), Instant.now());
            if (!duration.isNegative()) {
                timeToDone.record(duration);
            }
        }
    }

    public void adminLogin(boolean success) {
        Counter.builder("praxis.admin.logins")
                .description("Admin login attempts")
                .tag("result", success ? "success" : "failure")
                .register(registry)
                .increment();
    }

    public void attachmentDownloaded() {
        attachmentDownloads.increment();
    }

    /** Refreshes the database-backed gauges periodically instead of querying MongoDB on every scrape. */
    @Scheduled(fixedDelayString = "${app.metrics.refresh-interval-ms:30000}", initialDelay = 5000)
    public void refreshGauges() {
        try {
            for (Map.Entry<SubmissionStatus, AtomicLong> entry : submissionsByStatus.entrySet()) {
                entry.getValue().set(submissionRepository.countByStatus(entry.getKey()));
            }
            long oldestAge = submissionRepository.findFirstByStatusOrderByCreatedAtAsc(SubmissionStatus.NEW)
                    .map(Submission::getCreatedAt)
                    .map(createdAt -> Math.max(0, Duration.between(createdAt, Instant.now()).getSeconds()))
                    .orElse(0L);
            oldestNewSubmissionAgeSeconds.set(oldestAge);
        } catch (Exception ex) {
            log.warn("Could not refresh submission metrics: {}", ex.getMessage());
        }
    }
}
