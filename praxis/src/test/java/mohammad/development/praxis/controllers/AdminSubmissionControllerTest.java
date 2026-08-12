package mohammad.development.praxis.controllers;

import mohammad.development.praxis.DTOs.AdminSubmissionUpdateRequest;
import mohammad.development.praxis.DTOs.UpdateStatusRequest;
import mohammad.development.praxis.modules.patient.*;
import mohammad.development.praxis.repos.SubmissionRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.server.ResponseStatusException;
import com.mongodb.client.gridfs.model.GridFSFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminSubmissionControllerTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private GridFsTemplate gridFsTemplate;

    @InjectMocks
    private AdminSubmissionController controller;

    private Submission testSubmission;

    @BeforeEach
    void setUp() {
        testSubmission = new Submission();
        testSubmission.setId("test-id-123");
        testSubmission.setStatus(SubmissionStatus.NEW);
        testSubmission.setFormVersion("v1");
        testSubmission.setCreatedAt(Instant.now());
        testSubmission.setUpdatedAt(Instant.now());

        PatientData patientData = new PatientData();
        patientData.setFirstName("Max");
        patientData.setLastName("Mustermann");
        patientData.setBirthDate("1990-01-15");
        patientData.setPhone("+49123456789");
        patientData.setEmail("max@example.com");

        Address address = new Address();
        address.setStreet("Musterstraße");
        address.setHouseNumber("123");
        address.setZip("12345");
        address.setCity("Berlin");
        patientData.setAddress(address);

        testSubmission.setPatientData(patientData);

        MedicalData medicalData = new MedicalData();
        medicalData.setAllergies(List.of("Pollen"));
        medicalData.setMedications(List.of("Aspirin"));
        medicalData.setPreExistingConditions(List.of("None"));
        testSubmission.setMedical(medicalData);

        Consents consents = new Consents();
        consents.setGdprAccepted(true);
        consents.setDataSharingAccepted(true);
        consents.setAcceptedAt(Instant.now());
        testSubmission.setConsents(consents);
    }

    @Test
    void list_withoutStatus_returnsAllSubmissions() {
        Submission second = new Submission();
        second.setId("test-id-456");
        second.setCreatedAt(Instant.now().minusSeconds(3600));

        when(submissionRepository.findAll()).thenReturn(List.of(testSubmission, second));

        List<Submission> result = controller.list(null);

        assertEquals(2, result.size());
        assertEquals("test-id-123", result.get(0).getId());
    }

    @Test
    void list_withStatus_returnsFilteredSubmissions() {
        when(submissionRepository.findAllByStatusOrderByCreatedAtDesc(SubmissionStatus.NEW))
                .thenReturn(List.of(testSubmission));

        List<Submission> result = controller.list(SubmissionStatus.NEW);

        assertEquals(1, result.size());
        assertEquals(SubmissionStatus.NEW, result.get(0).getStatus());
    }

    @Test
    void get_existingSubmission_returnsSubmission() {
        when(submissionRepository.findById("test-id-123")).thenReturn(Optional.of(testSubmission));

        Submission result = controller.get("test-id-123");

        assertEquals("test-id-123", result.getId());
        assertEquals("Max", result.getPatientData().getFirstName());
    }

    @Test
    void get_nonExistingSubmission_throws404() {
        when(submissionRepository.findById("non-existing")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> controller.get("non-existing"));
    }

    @Test
    void update_existingSubmission_returnsUpdatedSubmission() {
        when(submissionRepository.findById("test-id-123")).thenReturn(Optional.of(testSubmission));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(i -> i.getArgument(0));

        AdminSubmissionUpdateRequest updateRequest = new AdminSubmissionUpdateRequest();
        PatientData updatedPatientData = new PatientData();
        updatedPatientData.setFirstName("Updated");
        updatedPatientData.setLastName("Name");
        updateRequest.setPatientData(updatedPatientData);

        Submission result = controller.update("test-id-123", updateRequest);

        assertEquals("Updated", result.getPatientData().getFirstName());
    }

    @Test
    void update_nonExistingSubmission_throws404() {
        when(submissionRepository.findById("non-existing")).thenReturn(Optional.empty());

        AdminSubmissionUpdateRequest updateRequest = new AdminSubmissionUpdateRequest();

        assertThrows(ResponseStatusException.class, () -> controller.update("non-existing", updateRequest));
    }

    @Test
    void updateStatus_validStatus_returnsUpdatedSubmission() {
        when(submissionRepository.findById("test-id-123")).thenReturn(Optional.of(testSubmission));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(i -> i.getArgument(0));

        UpdateStatusRequest statusRequest = new UpdateStatusRequest();
        statusRequest.setStatus(SubmissionStatus.DONE);

        Submission result = controller.updateStatus("test-id-123", statusRequest);

        assertEquals(SubmissionStatus.DONE, result.getStatus());
    }

    @Test
    void updateStatus_nullStatus_throws400() {
        UpdateStatusRequest statusRequest = new UpdateStatusRequest();
        statusRequest.setStatus(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.updateStatus("test-id-123", statusRequest));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void updateStatus_nonExistingSubmission_throws404() {
        when(submissionRepository.findById("non-existing")).thenReturn(Optional.empty());

        UpdateStatusRequest statusRequest = new UpdateStatusRequest();
        statusRequest.setStatus(SubmissionStatus.VIEWED);

        assertThrows(ResponseStatusException.class,
                () -> controller.updateStatus("non-existing", statusRequest));
    }

    @Test
    void downloadAttachment_existingAttachment_returnsFile() {
        String attachmentId = new ObjectId().toHexString();

        SubmissionAttachment attachment = new SubmissionAttachment();
        attachment.setId(attachmentId);
        attachment.setFileName("test.pdf");
        attachment.setContentType("application/pdf");
        attachment.setSize(1024);
        testSubmission.setAttachments(List.of(attachment));

        when(submissionRepository.findById("test-id-123")).thenReturn(Optional.of(testSubmission));

        GridFSFile gridFSFile = mock(GridFSFile.class);
        when(gridFsTemplate.findOne(any())).thenReturn(gridFSFile);

        GridFsResource resource = mock(GridFsResource.class);
        when(gridFsTemplate.getResource(gridFSFile)).thenReturn(resource);

        ResponseEntity<Resource> result = controller.downloadAttachment("test-id-123", attachmentId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("application/pdf", result.getHeaders().getContentType().toString());
    }

    @Test
    void downloadAttachment_nonExistingSubmission_throws404() {
        when(submissionRepository.findById("non-existing")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> controller.downloadAttachment("non-existing", "some-attachment-id"));
    }

    @Test
    void downloadAttachment_nonExistingAttachment_throws404() {
        testSubmission.setAttachments(List.of());
        when(submissionRepository.findById("test-id-123")).thenReturn(Optional.of(testSubmission));

        assertThrows(ResponseStatusException.class,
                () -> controller.downloadAttachment("test-id-123", "non-existing"));
    }

    @Test
    void downloadAttachment_fileNotInGridFs_throws404() {
        String attachmentId = new ObjectId().toHexString();

        SubmissionAttachment attachment = new SubmissionAttachment();
        attachment.setId(attachmentId);
        attachment.setFileName("test.pdf");
        attachment.setContentType("application/pdf");
        attachment.setSize(1024);
        testSubmission.setAttachments(List.of(attachment));

        when(submissionRepository.findById("test-id-123")).thenReturn(Optional.of(testSubmission));
        when(gridFsTemplate.findOne(any())).thenReturn(null);

        assertThrows(ResponseStatusException.class,
                () -> controller.downloadAttachment("test-id-123", attachmentId));
    }

    @Test
    void downloadAttachment_nullContentType_usesOctetStream() {
        String attachmentId = new ObjectId().toHexString();

        SubmissionAttachment attachment = new SubmissionAttachment();
        attachment.setId(attachmentId);
        attachment.setFileName("test.bin");
        attachment.setContentType(null);
        attachment.setSize(1024);
        testSubmission.setAttachments(List.of(attachment));

        when(submissionRepository.findById("test-id-123")).thenReturn(Optional.of(testSubmission));

        GridFSFile gridFSFile = mock(GridFSFile.class);
        when(gridFsTemplate.findOne(any())).thenReturn(gridFSFile);

        GridFsResource resource = mock(GridFsResource.class);
        when(gridFsTemplate.getResource(gridFSFile)).thenReturn(resource);

        ResponseEntity<Resource> result = controller.downloadAttachment("test-id-123", attachmentId);

        assertEquals("application/octet-stream", result.getHeaders().getContentType().toString());
    }

    @Test
    void downloadAttachment_nullFileName_usesDefaultFileName() {
        String attachmentId = new ObjectId().toHexString();

        SubmissionAttachment attachment = new SubmissionAttachment();
        attachment.setId(attachmentId);
        attachment.setFileName(null);
        attachment.setContentType("application/pdf");
        attachment.setSize(1024);
        testSubmission.setAttachments(List.of(attachment));

        when(submissionRepository.findById("test-id-123")).thenReturn(Optional.of(testSubmission));

        GridFSFile gridFSFile = mock(GridFSFile.class);
        when(gridFsTemplate.findOne(any())).thenReturn(gridFSFile);

        GridFsResource resource = mock(GridFsResource.class);
        when(gridFsTemplate.getResource(gridFSFile)).thenReturn(resource);

        ResponseEntity<Resource> result = controller.downloadAttachment("test-id-123", attachmentId);

        assertTrue(result.getHeaders().getContentDisposition().toString().contains("file"));
    }

    @Test
    void downloadAttachment_blankFileName_usesDefaultFileName() {
        String attachmentId = new ObjectId().toHexString();

        SubmissionAttachment attachment = new SubmissionAttachment();
        attachment.setId(attachmentId);
        attachment.setFileName("   ");
        attachment.setContentType("application/pdf");
        attachment.setSize(1024);
        testSubmission.setAttachments(List.of(attachment));

        when(submissionRepository.findById("test-id-123")).thenReturn(Optional.of(testSubmission));

        GridFSFile gridFSFile = mock(GridFSFile.class);
        when(gridFsTemplate.findOne(any())).thenReturn(gridFSFile);

        GridFsResource resource = mock(GridFsResource.class);
        when(gridFsTemplate.getResource(gridFSFile)).thenReturn(resource);

        ResponseEntity<Resource> result = controller.downloadAttachment("test-id-123", attachmentId);

        assertTrue(result.getHeaders().getContentDisposition().toString().contains("file"));
    }

    @Test
    void downloadAttachment_nullAttachmentsList_throws404() {
        testSubmission.setAttachments(null);
        when(submissionRepository.findById("test-id-123")).thenReturn(Optional.of(testSubmission));

        assertThrows(ResponseStatusException.class,
                () -> controller.downloadAttachment("test-id-123", "some-id"));
    }
}

