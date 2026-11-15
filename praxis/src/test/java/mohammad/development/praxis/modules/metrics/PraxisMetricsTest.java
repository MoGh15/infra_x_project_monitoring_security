package mohammad.development.praxis.modules.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import mohammad.development.praxis.modules.admin.SseHub;
import mohammad.development.praxis.modules.patient.Submission;
import mohammad.development.praxis.modules.patient.SubmissionStatus;
import mohammad.development.praxis.repos.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PraxisMetricsTest {

    private SimpleMeterRegistry registry;
    private SubmissionRepository repository;
    private SseHub sseHub;
    private PraxisMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        repository = mock(SubmissionRepository.class);
        sseHub = new SseHub();
        metrics = new PraxisMetrics(registry, repository, sseHub);
    }

    @Test
    void submissionCreated_incrementsCounterWithTags() {
        metrics.submissionCreated("v1", true);
        metrics.submissionCreated("v1", true);
        metrics.submissionCreated(null, false);

        assertEquals(2, registry.get("praxis.submissions.created")
                .tag("form_version", "v1").tag("has_attachments", "true").counter().count());
        assertEquals(1, registry.get("praxis.submissions.created")
                .tag("form_version", "unknown").tag("has_attachments", "false").counter().count());
    }

    @Test
    void submissionRejected_countsPerReason() {
        metrics.submissionRejected("file_too_large");

        assertEquals(1, registry.get("praxis.submissions.rejected")
                .tag("reason", "file_too_large").counter().count());
    }

    @Test
    void attachmentUploaded_recordsCountAndSize() {
        metrics.attachmentUploaded("application/pdf", 2048);

        assertEquals(1, registry.get("praxis.submission.attachments")
                .tag("content_type", "application/pdf").counter().count());
        assertEquals(2048, registry.get("praxis.submission.attachment.size").summary().totalAmount());
    }

    @Test
    void statusChanged_toDone_recordsTransitionAndTimeToDone() {
        Submission submission = new Submission();
        submission.setCreatedAt(Instant.now().minus(Duration.ofHours(2)));

        metrics.statusChanged(submission, SubmissionStatus.NEW, SubmissionStatus.DONE);

        assertEquals(1, registry.get("praxis.submission.status.changes")
                .tag("from", "NEW").tag("to", "DONE").counter().count());
        assertEquals(1, registry.get("praxis.submission.time.to.done").timer().count());
        assertTrue(registry.get("praxis.submission.time.to.done").timer().totalTime(TimeUnit.MINUTES) >= 119);
    }

    @Test
    void statusChanged_sameStatus_isIgnored() {
        metrics.statusChanged(new Submission(), SubmissionStatus.NEW, SubmissionStatus.NEW);

        assertNull(registry.find("praxis.submission.status.changes").counter());
    }

    @Test
    void adminLogin_countsSuccessAndFailure() {
        metrics.adminLogin(true);
        metrics.adminLogin(false);
        metrics.adminLogin(false);

        assertEquals(1, registry.get("praxis.admin.logins").tag("result", "success").counter().count());
        assertEquals(2, registry.get("praxis.admin.logins").tag("result", "failure").counter().count());
    }

    @Test
    void attachmentDownloaded_incrementsCounter() {
        metrics.attachmentDownloaded();

        assertEquals(1, registry.get("praxis.admin.attachment.downloads").counter().count());
    }

    @Test
    void sseGauge_reflectsConnectedClients() {
        assertEquals(0, registry.get("praxis.sse.connected.clients").gauge().value());

        sseHub.register();

        assertEquals(1, registry.get("praxis.sse.connected.clients").gauge().value());
    }

    @Test
    void refreshGauges_readsCountsAndOldestNewAge() {
        when(repository.countByStatus(SubmissionStatus.NEW)).thenReturn(3L);
        when(repository.countByStatus(SubmissionStatus.VIEWED)).thenReturn(1L);
        when(repository.countByStatus(SubmissionStatus.DONE)).thenReturn(10L);
        Submission oldest = new Submission();
        oldest.setCreatedAt(Instant.now().minus(Duration.ofHours(1)));
        when(repository.findFirstByStatusOrderByCreatedAtAsc(SubmissionStatus.NEW)).thenReturn(Optional.of(oldest));

        metrics.refreshGauges();

        assertEquals(3, registry.get("praxis.submissions.by.status").tag("status", "NEW").gauge().value());
        assertEquals(10, registry.get("praxis.submissions.by.status").tag("status", "DONE").gauge().value());
        assertTrue(registry.get("praxis.submissions.oldest.new.age").gauge().value() >= 3599);
    }

    @Test
    void refreshGauges_noNewSubmissions_ageIsZero() {
        when(repository.findFirstByStatusOrderByCreatedAtAsc(SubmissionStatus.NEW)).thenReturn(Optional.empty());

        metrics.refreshGauges();

        assertEquals(0, registry.get("praxis.submissions.oldest.new.age").gauge().value());
    }

    @Test
    void refreshGauges_repositoryFailure_doesNotThrow() {
        when(repository.countByStatus(any())).thenThrow(new RuntimeException("db down"));

        assertDoesNotThrow(() -> metrics.refreshGauges());
    }
}
