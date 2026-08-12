package mohammad.development.praxis.repos;

import mohammad.development.praxis.modules.patient.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionRepositoryTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Test
    void save_returnsSubmission() {
        Submission submission = createSubmission(SubmissionStatus.NEW);

        Submission savedSubmission = createSubmission(SubmissionStatus.NEW);
        savedSubmission.setId("generated-id");

        when(submissionRepository.save(submission)).thenReturn(savedSubmission);

        Submission result = submissionRepository.save(submission);

        assertNotNull(result.getId());
        assertEquals("generated-id", result.getId());
    }

    @Test
    void findById_existingSubmission_returnsSubmission() {
        Submission submission = createSubmission(SubmissionStatus.NEW);
        submission.setId("test-id");

        when(submissionRepository.findById("test-id")).thenReturn(Optional.of(submission));

        Optional<Submission> found = submissionRepository.findById("test-id");

        assertTrue(found.isPresent());
        assertEquals("test-id", found.get().getId());
    }

    @Test
    void findById_nonExistingSubmission_returnsEmpty() {
        when(submissionRepository.findById("nonexistent-id")).thenReturn(Optional.empty());

        Optional<Submission> found = submissionRepository.findById("nonexistent-id");

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_returnsAllSubmissions() {
        Submission s1 = createSubmission(SubmissionStatus.NEW);
        s1.setId("id-1");
        Submission s2 = createSubmission(SubmissionStatus.VIEWED);
        s2.setId("id-2");
        Submission s3 = createSubmission(SubmissionStatus.DONE);
        s3.setId("id-3");

        when(submissionRepository.findAll()).thenReturn(List.of(s1, s2, s3));

        List<Submission> all = submissionRepository.findAll();

        assertEquals(3, all.size());
    }

    @Test
    void findAllByStatusOrderByCreatedAtDesc_filtersByStatus() {
        Submission s1 = createSubmission(SubmissionStatus.NEW);
        s1.setId("id-1");
        Submission s2 = createSubmission(SubmissionStatus.NEW);
        s2.setId("id-2");

        when(submissionRepository.findAllByStatusOrderByCreatedAtDesc(SubmissionStatus.NEW))
                .thenReturn(List.of(s1, s2));

        List<Submission> newSubmissions = submissionRepository.findAllByStatusOrderByCreatedAtDesc(SubmissionStatus.NEW);

        assertEquals(2, newSubmissions.size());
        assertTrue(newSubmissions.stream().allMatch(s -> s.getStatus() == SubmissionStatus.NEW));
    }

    @Test
    void findAllByStatusOrderByCreatedAtDesc_noMatchingStatus_returnsEmpty() {
        when(submissionRepository.findAllByStatusOrderByCreatedAtDesc(SubmissionStatus.DONE))
                .thenReturn(List.of());

        List<Submission> doneSubmissions = submissionRepository.findAllByStatusOrderByCreatedAtDesc(SubmissionStatus.DONE);

        assertTrue(doneSubmissions.isEmpty());
    }

    @Test
    void delete_callsDelete() {
        Submission submission = createSubmission(SubmissionStatus.NEW);
        submission.setId("test-id");

        doNothing().when(submissionRepository).delete(submission);

        submissionRepository.delete(submission);

        verify(submissionRepository, times(1)).delete(submission);
    }

    @Test
    void deleteById_callsDeleteById() {
        doNothing().when(submissionRepository).deleteById("test-id");

        submissionRepository.deleteById("test-id");

        verify(submissionRepository, times(1)).deleteById("test-id");
    }

    @Test
    void count_returnsCorrectCount() {
        when(submissionRepository.count()).thenReturn(5L);

        assertEquals(5L, submissionRepository.count());
    }

    private Submission createSubmission(SubmissionStatus status) {
        Submission submission = new Submission();
        submission.setStatus(status);
        submission.setFormVersion("v1");

        PatientData patientData = new PatientData();
        patientData.setFirstName("Test");
        patientData.setLastName("User");
        patientData.setBirthDate("1990-01-01");
        patientData.setPhone("+49123456789");
        patientData.setEmail("test@example.com");

        Address address = new Address();
        address.setStreet("Teststraße");
        address.setHouseNumber("1");
        address.setZip("12345");
        address.setCity("Berlin");
        patientData.setAddress(address);

        submission.setPatientData(patientData);

        MedicalData medicalData = new MedicalData();
        medicalData.setAllergies(List.of("None"));
        submission.setMedical(medicalData);

        Consents consents = new Consents();
        consents.setGdprAccepted(true);
        consents.setAcceptedAt(Instant.now());
        submission.setConsents(consents);

        return submission;
    }
}

