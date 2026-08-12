package mohammad.development.praxis.modules.patient;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatientModelsTest {

    @Test
    void address_gettersAndSetters() {
        Address address = new Address();
        address.setStreet("Hauptstraße");
        address.setHouseNumber("42a");
        address.setZip("10115");
        address.setCity("Berlin");

        assertEquals("Hauptstraße", address.getStreet());
        assertEquals("42a", address.getHouseNumber());
        assertEquals("10115", address.getZip());
        assertEquals("Berlin", address.getCity());
    }

    @Test
    void consents_gettersAndSetters() {
        Consents consents = new Consents();
        Instant now = Instant.now();

        consents.setGdprAccepted(true);
        consents.setDataSharingAccepted(false);
        consents.setAcceptedAt(now);

        assertTrue(consents.isGdprAccepted());
        assertFalse(consents.isDataSharingAccepted());
        assertEquals(now, consents.getAcceptedAt());
    }

    @Test
    void medicalData_gettersAndSetters() {
        MedicalData medicalData = new MedicalData();

        List<String> allergies = List.of("Pollen", "Nüsse");
        List<String> medications = List.of("Aspirin");
        List<String> conditions = List.of("Diabetes");

        SymptomDetail symptom = new SymptomDetail();
        symptom.setKey("headache");
        symptom.setLabel("Kopfschmerzen");
        List<SymptomDetail> symptoms = List.of(symptom);

        medicalData.setAllergies(allergies);
        medicalData.setMedications(medications);
        medicalData.setPreExistingConditions(conditions);
        medicalData.setSymptoms(symptoms);

        assertEquals(2, medicalData.getAllergies().size());
        assertEquals(1, medicalData.getMedications().size());
        assertEquals(1, medicalData.getPreExistingConditions().size());
        assertEquals(1, medicalData.getSymptoms().size());
        assertEquals("headache", medicalData.getSymptoms().get(0).getKey());
    }

    @Test
    void patientData_gettersAndSetters() {
        PatientData patientData = new PatientData();

        Address address = new Address();
        address.setCity("München");

        patientData.setFirstName("Max");
        patientData.setLastName("Mustermann");
        patientData.setBirthDate("1990-05-15");
        patientData.setPhone("+49123456789");
        patientData.setEmail("max@example.com");
        patientData.setAddress(address);

        assertEquals("Max", patientData.getFirstName());
        assertEquals("Mustermann", patientData.getLastName());
        assertEquals("1990-05-15", patientData.getBirthDate());
        assertEquals("+49123456789", patientData.getPhone());
        assertEquals("max@example.com", patientData.getEmail());
        assertEquals("München", patientData.getAddress().getCity());
    }

    @Test
    void signature_gettersAndSetters() {
        Signature signature = new Signature();

        SignatureStroke stroke = new SignatureStroke();
        SignaturePoint point = new SignaturePoint();
        point.setX(10.0);
        point.setY(20.0);
        point.setT(1000L);
        stroke.setPoints(List.of(point));
        stroke.setWidth(2.0);

        signature.setContentType("image/png");
        signature.setBase64("iVBORw0KGgoAAAANSUhEUg==");
        signature.setStrokes(List.of(stroke));

        assertEquals("image/png", signature.getContentType());
        assertEquals("iVBORw0KGgoAAAANSUhEUg==", signature.getBase64());
        assertEquals(1, signature.getStrokes().size());
        assertEquals(10.0, signature.getStrokes().get(0).getPoints().get(0).getX());
    }

    @Test
    void signaturePoint_gettersAndSetters() {
        SignaturePoint point = new SignaturePoint();

        point.setX(100.5);
        point.setY(200.75);
        point.setT(12345L);

        assertEquals(100.5, point.getX());
        assertEquals(200.75, point.getY());
        assertEquals(12345L, point.getT());
    }

    @Test
    void signatureStroke_gettersAndSetters() {
        SignatureStroke stroke = new SignatureStroke();

        SignaturePoint p1 = new SignaturePoint();
        p1.setX(0);
        p1.setY(0);

        SignaturePoint p2 = new SignaturePoint();
        p2.setX(10);
        p2.setY(10);

        stroke.setPoints(List.of(p1, p2));
        stroke.setWidth(3.5);

        assertEquals(2, stroke.getPoints().size());
        assertEquals(3.5, stroke.getWidth());
    }

    @Test
    void submission_gettersAndSetters() {
        Submission submission = new Submission();
        Instant now = Instant.now();

        PatientData patientData = new PatientData();
        patientData.setFirstName("Test");

        MedicalData medicalData = new MedicalData();
        Consents consents = new Consents();
        Signature signature = new Signature();
        SubmissionMeta meta = new SubmissionMeta();

        SubmissionAttachment attachment = new SubmissionAttachment();
        attachment.setId("att-1");

        submission.setId("sub-123");
        submission.setCreatedAt(now);
        submission.setUpdatedAt(now);
        submission.setStatus(SubmissionStatus.VIEWED);
        submission.setFormVersion("v2");
        submission.setPatientData(patientData);
        submission.setMedical(medicalData);
        submission.setConsents(consents);
        submission.setSignature(signature);
        submission.setMeta(meta);
        submission.setAttachments(List.of(attachment));

        assertEquals("sub-123", submission.getId());
        assertEquals(now, submission.getCreatedAt());
        assertEquals(now, submission.getUpdatedAt());
        assertEquals(SubmissionStatus.VIEWED, submission.getStatus());
        assertEquals("v2", submission.getFormVersion());
        assertEquals("Test", submission.getPatientData().getFirstName());
        assertNotNull(submission.getMedical());
        assertNotNull(submission.getConsents());
        assertNotNull(submission.getSignature());
        assertNotNull(submission.getMeta());
        assertEquals(1, submission.getAttachments().size());
    }

    @Test
    void submission_defaultValues() {
        Submission submission = new Submission();

        assertEquals(SubmissionStatus.NEW, submission.getStatus());
        assertEquals("v1", submission.getFormVersion());
        assertNotNull(submission.getAttachments());
        assertTrue(submission.getAttachments().isEmpty());
    }

    @Test
    void submissionAttachment_gettersAndSetters() {
        SubmissionAttachment attachment = new SubmissionAttachment();
        Instant now = Instant.now();

        attachment.setId("att-123");
        attachment.setFileName("document.pdf");
        attachment.setContentType("application/pdf");
        attachment.setSize(1024L);
        attachment.setUploadedAt(now);

        assertEquals("att-123", attachment.getId());
        assertEquals("document.pdf", attachment.getFileName());
        assertEquals("application/pdf", attachment.getContentType());
        assertEquals(1024L, attachment.getSize());
        assertEquals(now, attachment.getUploadedAt());
    }

    @Test
    void submissionMeta_gettersAndSetters() {
        SubmissionMeta meta = new SubmissionMeta();

        meta.setTabletId("tablet-001");
        meta.setLanguage("de");
        meta.setUserAgent("Mozilla/5.0");
        meta.setIp("192.168.1.1");

        assertEquals("tablet-001", meta.getTabletId());
        assertEquals("de", meta.getLanguage());
        assertEquals("Mozilla/5.0", meta.getUserAgent());
        assertEquals("192.168.1.1", meta.getIp());
    }

    @Test
    void submissionStatus_values() {
        SubmissionStatus[] values = SubmissionStatus.values();

        assertEquals(3, values.length);
        assertEquals(SubmissionStatus.NEW, SubmissionStatus.valueOf("NEW"));
        assertEquals(SubmissionStatus.VIEWED, SubmissionStatus.valueOf("VIEWED"));
        assertEquals(SubmissionStatus.DONE, SubmissionStatus.valueOf("DONE"));
    }

    @Test
    void symptomDetail_gettersAndSetters() {
        SymptomDetail symptom = new SymptomDetail();

        symptom.setKey("fever");
        symptom.setLabel("Fieber");
        symptom.setOption("high");
        symptom.setSeverity(8);
        symptom.setOnset("2024-01-15");
        symptom.setNotes("Seit 3 Tagen");

        assertEquals("fever", symptom.getKey());
        assertEquals("Fieber", symptom.getLabel());
        assertEquals("high", symptom.getOption());
        assertEquals(8, symptom.getSeverity());
        assertEquals("2024-01-15", symptom.getOnset());
        assertEquals("Seit 3 Tagen", symptom.getNotes());
    }
}

