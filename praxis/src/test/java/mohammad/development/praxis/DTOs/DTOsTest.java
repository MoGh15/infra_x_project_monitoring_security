package mohammad.development.praxis.DTOs;

import mohammad.development.praxis.modules.patient.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DTOsTest {

    @Test
    void loginRequest_gettersAndSetters() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("password123");

        assertEquals("admin", request.getUsername());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void loginResponse_constructor() {
        LoginResponse response = new LoginResponse("jwt-token-123");

        assertEquals("jwt-token-123", response.getAccessToken());
    }

    @Test
    void updateStatusRequest_gettersAndSetters() {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(SubmissionStatus.DONE);

        assertEquals(SubmissionStatus.DONE, request.getStatus());
    }

    @Test
    void updateStatusRequest_nullStatus() {
        UpdateStatusRequest request = new UpdateStatusRequest();

        assertNull(request.getStatus());
    }

    @Test
    void adminSubmissionUpdateRequest_gettersAndSetters() {
        AdminSubmissionUpdateRequest request = new AdminSubmissionUpdateRequest();

        PatientData patientData = new PatientData();
        patientData.setFirstName("Max");

        MedicalData medicalData = new MedicalData();

        Consents consents = new Consents();
        consents.setGdprAccepted(true);

        request.setPatientData(patientData);
        request.setMedical(medicalData);
        request.setConsents(consents);

        assertEquals("Max", request.getPatientData().getFirstName());
        assertNotNull(request.getMedical());
        assertTrue(request.getConsents().isGdprAccepted());
    }

    @Test
    void submissionCreateRequest_gettersAndSetters() {
        SubmissionCreateRequest request = new SubmissionCreateRequest();

        request.setFormVersion("v2");

        PatientData patientData = new PatientData();
        patientData.setFirstName("Anna");
        request.setPatientData(patientData);

        MedicalData medicalData = new MedicalData();
        request.setMedical(medicalData);

        Consents consents = new Consents();
        request.setConsents(consents);

        Signature signature = new Signature();
        signature.setContentType("image/png");
        request.setSignature(signature);

        SubmissionMeta meta = new SubmissionMeta();
        meta.setLanguage("de");
        request.setMeta(meta);

        assertEquals("v2", request.getFormVersion());
        assertEquals("Anna", request.getPatientData().getFirstName());
        assertNotNull(request.getMedical());
        assertNotNull(request.getConsents());
        assertEquals("image/png", request.getSignature().getContentType());
        assertEquals("de", request.getMeta().getLanguage());
    }

    @Test
    void submissionCreateRequest_nullFormVersion_usesNull() {
        SubmissionCreateRequest request = new SubmissionCreateRequest();

        assertNull(request.getFormVersion());
    }
}

