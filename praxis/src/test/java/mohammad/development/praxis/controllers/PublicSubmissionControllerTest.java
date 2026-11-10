package mohammad.development.praxis.controllers;

import mohammad.development.praxis.DTOs.SubmissionCreateRequest;
import mohammad.development.praxis.modules.admin.SseHub;
import mohammad.development.praxis.modules.patient.*;
import mohammad.development.praxis.repos.SubmissionRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicSubmissionControllerTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private GridFsTemplate gridFsTemplate;

    @Mock
    private SseHub sseHub;

    @InjectMocks
    private PublicSubmissionController controller;

    private SubmissionCreateRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new SubmissionCreateRequest();
        validRequest.setFormVersion("v1");

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
        validRequest.setPatientData(patientData);

        MedicalData medicalData = new MedicalData();
        medicalData.setAllergies(List.of("Pollen"));
        medicalData.setMedications(List.of("Aspirin"));
        medicalData.setPreExistingConditions(List.of("None"));
        validRequest.setMedical(medicalData);

        Consents consents = new Consents();
        consents.setGdprAccepted(true);
        consents.setDataSharingAccepted(true);
        consents.setAcceptedAt(Instant.now());
        validRequest.setConsents(consents);

        Signature signature = new Signature();
        signature.setContentType("image/png");
        signature.setBase64("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");
        validRequest.setSignature(signature);

        SubmissionMeta meta = new SubmissionMeta();
        meta.setTabletId("tablet-1");
        meta.setLanguage("de");
        validRequest.setMeta(meta);
    }

    @Test
    void submit_validRequest_createsSubmission() {
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> {
            Submission s = invocation.getArgument(0);
            s.setId("generated-id");
            s.setCreatedAt(Instant.now());
            return s;
        });

        Submission result = controller.submit(validRequest);

        assertEquals("generated-id", result.getId());
        assertEquals(SubmissionStatus.NEW, result.getStatus());
        assertEquals("Max", result.getPatientData().getFirstName());
        verify(sseHub, times(1)).sendCreated(any());
    }

    @Test
    void submit_withoutFormVersion_usesDefaultV1() {
        validRequest.setFormVersion(null);

        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> {
            Submission s = invocation.getArgument(0);
            s.setId("generated-id");
            return s;
        });

        Submission result = controller.submit(validRequest);

        assertEquals("v1", result.getFormVersion());
    }

    @Test
    void submit_withoutSignature_throws400() {
        validRequest.setSignature(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.submit(validRequest));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void submit_signatureWithoutContentType_throws400() {
        Signature signature = new Signature();
        signature.setContentType(null);
        signature.setBase64("someBase64Data");
        validRequest.setSignature(signature);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.submit(validRequest));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void submit_signatureWithBlankContentType_throws400() {
        Signature signature = new Signature();
        signature.setContentType("   ");
        signature.setBase64("someBase64Data");
        validRequest.setSignature(signature);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.submit(validRequest));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void submit_signatureWithoutBase64_throws400() {
        Signature signature = new Signature();
        signature.setContentType("image/png");
        signature.setBase64(null);
        validRequest.setSignature(signature);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.submit(validRequest));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void submit_signatureWithBlankBase64_throws400() {
        Signature signature = new Signature();
        signature.setContentType("image/png");
        signature.setBase64("  ");
        validRequest.setSignature(signature);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.submit(validRequest));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void submitWithFiles_validRequest_createsSubmissionWithAttachments() throws Exception {
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> {
            Submission s = invocation.getArgument(0);
            s.setId("generated-id");
            s.setCreatedAt(Instant.now());
            return s;
        });
        when(gridFsTemplate.store(any(InputStream.class), anyString(), anyString(), any()))
                .thenReturn(new ObjectId());

        MockMultipartFile filePart = new MockMultipartFile(
                "files",
                "test.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        Submission result = controller.submitWithFiles(validRequest, List.of(filePart));

        assertEquals("generated-id", result.getId());
        assertFalse(result.getAttachments().isEmpty());
        verify(sseHub, times(1)).sendCreated(any());
    }

    @Test
    void submitWithFiles_withoutFiles_createsSubmissionWithoutAttachments() {
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> {
            Submission s = invocation.getArgument(0);
            s.setId("generated-id");
            return s;
        });

        Submission result = controller.submitWithFiles(validRequest, null);

        assertEquals("generated-id", result.getId());
        verify(gridFsTemplate, never()).store(any(InputStream.class), anyString(), anyString(), any());
    }

    @Test
    void submitWithFiles_emptyFilesList_createsSubmission() {
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> {
            Submission s = invocation.getArgument(0);
            s.setId("generated-id");
            return s;
        });

        Submission result = controller.submitWithFiles(validRequest, new ArrayList<>());

        assertEquals("generated-id", result.getId());
        verify(sseHub, times(1)).sendCreated(any());
    }

    @Test
    void submitWithFiles_tooManyFiles_throws400() {
        List<MultipartFile> files = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            files.add(new MockMultipartFile("files", i + ".pdf", "application/pdf", "content".getBytes()));
        }

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.submitWithFiles(validRequest, files));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void submitWithFiles_unsupportedFileType_throws400() {
        MockMultipartFile unsupportedFile = new MockMultipartFile(
                "files",
                "test.exe",
                "application/x-msdownload",
                "content".getBytes()
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.submitWithFiles(validRequest, List.of(unsupportedFile)));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void submitWithFiles_emptyFile_throws400() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "files",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.submitWithFiles(validRequest, List.of(emptyFile)));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void submitWithFiles_nullContentType_throws400() {
        MockMultipartFile nullTypeFile = new MockMultipartFile(
                "files",
                "test.pdf",
                null,
                "content".getBytes()
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.submitWithFiles(validRequest, List.of(nullTypeFile)));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void submitWithFiles_fileUploadFails_cleansUpAndThrows400() throws Exception {
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> {
            Submission s = invocation.getArgument(0);
            s.setId("generated-id");
            return s;
        });
        when(gridFsTemplate.store(any(InputStream.class), anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Storage failure"));

        MockMultipartFile filePart = new MockMultipartFile(
                "files",
                "test.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        assertThrows(ResponseStatusException.class,
                () -> controller.submitWithFiles(validRequest, List.of(filePart)));

        verify(submissionRepository, times(1)).deleteById("generated-id");
    }

    @Test
    void submitWithFiles_allowedTypes_jpeg() throws Exception {
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> {
            Submission s = invocation.getArgument(0);
            s.setId("generated-id");
            return s;
        });
        when(gridFsTemplate.store(any(InputStream.class), anyString(), anyString(), any()))
                .thenReturn(new ObjectId());

        MockMultipartFile jpegFile = new MockMultipartFile(
                "files",
                "image.jpg",
                "image/jpeg",
                "JPEG content".getBytes()
        );

        Submission result = controller.submitWithFiles(validRequest, List.of(jpegFile));

        assertEquals("generated-id", result.getId());
    }

    @Test
    void submitWithFiles_allowedTypes_png() throws Exception {
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> {
            Submission s = invocation.getArgument(0);
            s.setId("generated-id");
            return s;
        });
        when(gridFsTemplate.store(any(InputStream.class), anyString(), anyString(), any()))
                .thenReturn(new ObjectId());

        MockMultipartFile pngFile = new MockMultipartFile(
                "files",
                "image.png",
                "image/png",
                "PNG content".getBytes()
        );

        Submission result = controller.submitWithFiles(validRequest, List.of(pngFile));

        assertEquals("generated-id", result.getId());
    }

    @Test
    void submitWithFiles_allowedTypes_webp() throws Exception {
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> {
            Submission s = invocation.getArgument(0);
            s.setId("generated-id");
            return s;
        });
        when(gridFsTemplate.store(any(InputStream.class), anyString(), anyString(), any()))
                .thenReturn(new ObjectId());

        MockMultipartFile webpFile = new MockMultipartFile(
                "files",
                "image.webp",
                "image/webp",
                "WebP content".getBytes()
        );

        Submission result = controller.submitWithFiles(validRequest, List.of(webpFile));

        assertEquals("generated-id", result.getId());
    }
}
